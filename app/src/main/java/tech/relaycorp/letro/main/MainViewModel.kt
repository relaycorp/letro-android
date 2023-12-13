package tech.relaycorp.letro.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaInitializationState
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.conversation.attachments.AttachmentsRepository
import tech.relaycorp.letro.conversation.attachments.dto.AttachmentToShare
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverter
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepository
import tech.relaycorp.letro.main.di.MainViewModelActionProcessorThread
import tech.relaycorp.letro.main.di.RootNavigationDebounceMs
import tech.relaycorp.letro.main.di.TermsAndConditionsLink
import tech.relaycorp.letro.ui.navigation.Action
import tech.relaycorp.letro.ui.navigation.RootNavigationScreen
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.coroutines.Dispatchers
import tech.relaycorp.letro.utils.ext.emitOn
import tech.relaycorp.letro.utils.navigation.UriToActionConverter
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@OptIn(FlowPreview::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val awalaManager: AwalaManager,
    private val accountRepository: AccountRepository,
    private val contactsRepository: ContactsRepository,
    private val attachmentsRepository: AttachmentsRepository,
    private val fileConverter: FileConverter,
    private val conversationsRepository: ConversationsRepository,
    private val uriToActionConverter: UriToActionConverter,
    private val logger: Logger,
    private val dispatchers: Dispatchers,
    @MainViewModelActionProcessorThread private val actionProcessorThread: CoroutineContext,
    @TermsAndConditionsLink private val termsAndConditionsLink: String,
    @RootNavigationDebounceMs private val rootNavigationDebounceMs: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState>
        get() = _uiState

    private val _openLinkSignal = MutableSharedFlow<String>()
    val openLinkSignal: SharedFlow<String>
        get() = _openLinkSignal

    private val _joinMeOnLetroSignal = MutableSharedFlow<String>()
    val joinMeOnLetroSignal: SharedFlow<String>
        get() = _joinMeOnLetroSignal

    private val _openFileSignal = MutableSharedFlow<File.FileWithoutContent>()
    val openFileSignal: SharedFlow<File.FileWithoutContent>
        get() = _openFileSignal

    private val _rootNavigationScreen: MutableStateFlow<RootNavigationScreen> =
        MutableStateFlow(RootNavigationScreen.AwalaInitializing)
    val rootNavigationScreen: StateFlow<RootNavigationScreen> get() = _rootNavigationScreen

    private val isAwalaInitialised = MutableStateFlow(false)
    private val accountIsReadyToProcessActions = MutableStateFlow<Boolean>(false)

    private val _clearBackstackSignal = MutableSharedFlow<RootNavigationScreen>()
    val clearBackstackSignal: MutableSharedFlow<RootNavigationScreen>
        get() = _clearBackstackSignal

    private val _actions = Channel<Action>()
    val actions: Flow<Action>
        get() = _actions.receiveAsFlow()

    private var currentAccount: Account? = null

    /**
     * TODO: refactor it
     * Navigation of the app is based on Root screens, managed by this view model.
     *
     * This variable needed to fix the problem https://relaycorp.atlassian.net/browse/LTR-136:
     * The problem happened when configuration was changed (screen rotation/switching between dark/light mode):
     * in this case, view subscribed to the StateFlow of root navigation screen, and navigated to it with popping the backstack, which resulted to losing state.
     *
     * Now, a subscriber of the root navigation screen flow must check this variable, and update it by calling [onRootNavigationScreenHandled], to not handle the same root navigation twice.
     */
    var rootNavigationScreenAlreadyHandled: Boolean = true
        private set

    /**
     * Used to figure out do we need to clear backstack after navigation event. We need to clear it, when account was changed
     */
    private var navigationHandledWithLastAccount: Long? = null

    init {
        viewModelScope.launch {
            contactsRepository.getContacts("vfsh@applepie.rocks").collect {
                it.forEach {
                    conversationsRepository.createNewConversation(
                        "vfsh@applepie.rocks",
                        recipient = it,
                        "<!DOCTYPE html>\n" +
                                "<html>\n" +
                                "\n" +
                                "<head>\n" +
                                "    <title> TEST HTML PAGE </title>\n" +
                                "    <meta charset=\"UTF-8\">\n" +
                                "    <meta name=\"description\" content=\"Most of HTML5 tags\">\n" +
                                "    <meta name=\"keywords\" content=\"HTML5, tags\">\n" +
                                "    <meta name=\"author\" content=\"http://blazardsky.space\">\n" +
                                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                                "</head>\n" +
                                "\n" +
                                "<body>\n" +
                                "    <header>\n" +
                                "        <nav>\n" +
                                "            <p>HEADER</p>\n" +
                                "            <menu type=\"context\" id=\"navmenu\">\n" +
                                "                <menuitem label=\"Home\" icon=\"icon.png\"> <a href=\"#\">Home</a> </menuitem>\n" +
                                "            </menu>\n" +
                                "        </nav>\n" +
                                "    </header>\n" +
                                "    <main>\n" +
                                "        <h1> Heading... </h1>\n" +
                                "        <h2> Heading... </h2>\n" +
                                "        <h3> Heading... </h3>\n" +
                                "        <h4> Heading... </h4>\n" +
                                "        <h5> Heading... </h5>\n" +
                                "        <h6> Heading... </h6>\n" +
                                "        <p>\n" +
                                "            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus nisi lacus, auctor sit amet purus vel, gravida luctus lectus. Aenean rhoncus dapibus enim, sit amet faucibus leo ornare vitae. <br>\n" +
                                "            <span> span </span>\n" +
                                "            <b>Bold word</b>\n" +
                                "            <i>italic</i>\n" +
                                "            <em>emphasis</em>\n" +
                                "            <mark>mark</mark>\n" +
                                "            <small> small </small>\n" +
                                "            <sub> sub </sub>\n" +
                                "            <sup> sup </sup>\n" +
                                "            <u> Statements... </u>\n" +
                                "            <abbr title=\"National Aeronautics and Space Administration\">NASA</abbr>\n" +
                                "            <strike> strikethrough </strike>\n" +
                                "            <span><del> deprecated info </del> <ins> new info </ins> </span>\n" +
                                "            <s> not relevant </s>\n" +
                                "            <a href=\"#link\">link</a>\n" +
                                "            <time datetime=\"2020-08-17 08:00\">Monday at 8:00 AM</time>\n" +
                                "            <ruby>\n" +
                                "                <rb>ruby base<rt>annotation\n" +
                                "            </ruby>\n" +
                                "            <br>\n" +
                                "            <kbd>CTRL</kbd>+<kbd>ALT</kbd>+<kbd>CANC</kbd>\n" +
                                "        </p>\n" +
                                "    </main>\n" +
                                "\n" +
                                "    <p> This is a <q>short quote</q> </p>\n" +
                                "    <blockquote> This instead is a long quote that is going to use a lot of words and also cite who said that. —<cite>Some People</cite> </blockquote>\n" +
                                "\n" +
                                "    <ol>\n" +
                                "        <li><data value=\"21053\">data tag</data></li>\n" +
                                "        <li><data value=\"23452\">data tag</data></li>\n" +
                                "        <li><data value=\"42545\">data tag</data></li>\n" +
                                "        <li>List item</li>\n" +
                                "        <li>List item</li>\n" +
                                "        <li>List item</li>\n" +
                                "    </ol>\n" +
                                "\n" +
                                "    <ul>\n" +
                                "        <li>List item</li>\n" +
                                "        <li>List item</li>\n" +
                                "        <li>List item</li>\n" +
                                "        <li>List item</li>\n" +
                                "        <li>List item</li>\n" +
                                "        <li>List item</li>\n" +
                                "    </ul>\n" +
                                "\n" +
                                "    <hr>\n" +
                                "\n" +
                                "    <template>\n" +
                                "        <h2>Hidden content (after page loaded).</h2>\n" +
                                "    </template>\n" +
                                "\n" +
                                "    <video width=\"640\" height=\"480\" src=\"https://archive.org/download/Popeye_forPresident/Popeye_forPresident_512kb.mp4\" controls>\n" +
                                "        <track kind=\"subtitles\" src=\"subtitles_de.vtt\" srclang=\"de\">\n" +
                                "        <track kind=\"subtitles\" src=\"subtitles_en.vtt\" srclang=\"en\">\n" +
                                "        <track kind=\"subtitles\" src=\"subtitles_ja.vtt\" srclang=\"ja\">\n" +
                                "        Sorry, your browser doesn't support HTML5 <code>video</code>, but you can\n" +
                                "        download this video from the <a href=\"https://archive.org/details/Popeye_forPresident\" target=\"_blank\">Internet Archive</a>.\n" +
                                "    </video>\n" +
                                "\n" +
                                "    <object data=\"flashmovie.swf\" width=\"600\" height=\"800\" type=\"application/x-shockwave-flash\">\n" +
                                "        Please install the Shockwave plugin to watch this movie.\n" +
                                "    </object>\n" +
                                "\n" +
                                "    <pre>\n" +
                                "\n" +
                                "                                                                             _,'/\n" +
                                "                                                                    _.-''._:\n" +
                                "                                                    ,-:`-.-'    .:.|\n" +
                                "                                                 ;-.''       .::.|\n" +
                                "                    _..------.._  / (:.       .:::.|\n" +
                                "             ,'.   .. . .  .`/  : :.     .::::.|\n" +
                                "         ,'. .    .  .   ./    \\ ::. .::::::.|\n" +
                                "     ,'. .  .    .   . /      `.,,::::::::.;\\\n" +
                                "    /  .            . /       ,',';_::::::,:_:\n" +
                                " / . .  .   .      /      ,',','::`--'':;._;\n" +
                                ": .             . /     ,',',':::::::_:'_,'\n" +
                                "|..  .   .   .   /    ,',','::::::_:'_,'\n" +
                                "|.              /,-. /,',':::::_:'_,'\n" +
                                "| ..    .    . /) /-:/,'::::_:',-'\n" +
                                ": . .     .   // / ,'):::_:',' ;\n" +
                                " \\ .   .     // /,' /,-.','  ./\n" +
                                "    \\ . .  `::./,// ,'' ,'   . /\n" +
                                "     `. .   . `;;;,/_.'' . . ,'\n" +
                                "        ,`. .   :;;' `:.  .  ,'\n" +
                                "     /   `-._,'  ..  ` _.-'\n" +
                                "    (     _,'``------'' \n" +
                                "     `--''\n" +
                                "\n" +
                                "    </pre>\n" +
                                "\n" +
                                "    <code>\n" +
                                "        // code tag\n" +
                                "        #include <iostream>\n" +
                                "\n" +
                                "            using namespace std;\n" +
                                "\n" +
                                "            int main()\n" +
                                "            {\n" +
                                "            cout << \"Hello World!\" << endl; return 0; } </code> <p>\n" +
                                "                <var> variable </var> = 1000;\n" +
                                "                <samp>Traceback (most recent call last):<br>NameError: name 'variabl' is not defined</samp>\n" +
                                "                </p>\n" +
                                "                <table>\n" +
                                "                    <thead>\n" +
                                "                        <tr>\n" +
                                "                            <th>Numbers</th>\n" +
                                "                            <th>Letters</th>\n" +
                                "                            <th>Colors</th>\n" +
                                "                        </tr>\n" +
                                "                    </thead>\n" +
                                "                    <tfoot>\n" +
                                "                        <tr>\n" +
                                "                            <td>123</td>\n" +
                                "                            <td>ABC</td>\n" +
                                "                            <td>RGB</td>\n" +
                                "                        </tr>\n" +
                                "                    </tfoot>\n" +
                                "                    <tbody>\n" +
                                "                        <tr>\n" +
                                "                            <td>1</td>\n" +
                                "                            <td>A</td>\n" +
                                "                            <td>Red</td>\n" +
                                "                        </tr>\n" +
                                "                        <tr>\n" +
                                "                            <td>2</td>\n" +
                                "                            <td>B</td>\n" +
                                "                            <td>Green</td>\n" +
                                "                        </tr>\n" +
                                "                        <tr>\n" +
                                "                            <td>3</td>\n" +
                                "                            <td>C</td>\n" +
                                "                            <td>Blue</td>\n" +
                                "                        </tr>\n" +
                                "                    </tbody>\n" +
                                "                </table>\n" +
                                "\n" +
                                "                <p> A <dfn>definition</dfn> is an explanation of the meaning of a word or phrase. </p>\n" +
                                "\n" +
                                "                <details>\n" +
                                "                    <summary>Summary of content below</summary>\n" +
                                "                    <p>Content 1</p>\n" +
                                "                    <p>Content 2</p>\n" +
                                "                    <p>Content 3</p>\n" +
                                "                    <p>Content 4</p>\n" +
                                "                </details>\n" +
                                "                <section>\n" +
                                "                    <h1>Content</h1>\n" +
                                "                    <p>Informations about content.</p>\n" +
                                "                </section>\n" +
                                "\n" +
                                "                <progress value=\"33\" max=\"100\"></progress>\n" +
                                "                <meter value=\"11\" min=\"0\" max=\"45\" optimum=\"40\">25 out of 45</meter>\n" +
                                "\n" +
                                "                <p> 2+2 = <output>4</output> </p>\n" +
                                "\n" +
                                "                <select>\n" +
                                "                    <optgroup label=\"Choice [1-3]\">\n" +
                                "                        <option value=\"1\">One</option>\n" +
                                "                        <option value=\"2\">Two</option>\n" +
                                "                        <option value=\"3\">Three</option>\n" +
                                "                    </optgroup>\n" +
                                "                    <optgroup label=\"Choice [4-6]\">\n" +
                                "                        <option value=\"4\">Four</option>\n" +
                                "                        <option value=\"5\">Five</option>\n" +
                                "                        <option value=\"6\">Six</option>\n" +
                                "                    </optgroup>\n" +
                                "                </select>\n" +
                                "\n" +
                                "                <div>\n" +
                                "                    <div>\n" +
                                "                        <p> div > div > p </p>\n" +
                                "                    </div>\n" +
                                "\n" +
                                "                    <br>\n" +
                                "\n" +
                                "\n" +
                                "                </div>\n" +
                                "                <svg width=\"100\" height=\"100\">\n" +
                                "                    <circle cx=\"50\" cy=\"50\" r=\"40\" stroke=\"green\" stroke-width=\"4\" fill=\"yellow\" />\n" +
                                "                </svg>\n" +
                                "\n" +
                                "                <br>\n" +
                                "\n" +
                                "                <textarea id=\"textarea\" name=\"textarea\" rows=\"4\" cols=\"50\">\n" +
                                "        Write something in here\n" +
                                "    </textarea>\n" +
                                "\n" +
                                "                <br>\n" +
                                "\n" +
                                "                <audio controls>\n" +
                                "                    I'm sorry. You're browser doesn't support HTML5 <code>audio</code>.\n" +
                                "                    <source src=\"https://archive.org/download/ReclaimHtml5/ReclaimHtml5.ogg\" type=\"audio/ogg\">\n" +
                                "                    <source src=\"https://archive.org/download/ReclaimHtml5/ReclaimHtml5.mp3\" type=\"audio/mpeg\">\n" +
                                "                </audio>\n" +
                                "                <p>This is a recording of a talk called <cite>Reclaim HTML5</cite> which was orinally delieved in Vancouver at a <a href=\"http://www.meetup.com/vancouver-javascript-developers/\" taget=\"_blank\">Super VanJS Meetup</a>. It is hosted by <a href=\"https://archive.org/details/ReclaimHtml5\"\n" +
                                "                     target=\"_blank\">The Internet Archive</a> and licensed under <a href=\"http://creativecommons.org/licenses/by/3.0/legalcode\" target=\"_blank\">CC 3.0</a>.</p>\n" +
                                "\n" +
                                "                <iframe src=\"https://open.spotify.com/embed?uri=spotify%3Atrack%3A67HxeUADW4H3ERfaPW59ma?si=PogFcGg9QqapyoPbn2lVOw\" width=\"300\" height=\"380\" frameborder=\"0\" allowtransparency=\"true\"></iframe>\n" +
                                "\n" +
                                "                <article>\n" +
                                "                    <header>\n" +
                                "                        <h2>Title of Article</h2>\n" +
                                "                        <span>by Arthur T. Writer</span>\n" +
                                "                    </header>\n" +
                                "                    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam volutpat sollicitudin nisi, at convallis nunc semper et. Donec ultrices odio ac purus facilisis, at mollis urna finibus.</p>\n" +
                                "                    <figure>\n" +
                                "                        <img src=\"https://placehold.it/600x300\" alt=\"placeholder-image\">\n" +
                                "                        <figcaption> Caption.</figcaption>\n" +
                                "                    </figure>\n" +
                                "                    <footer>\n" +
                                "                        <dl> <dt>Published</dt>\n" +
                                "                            <dd>17 August 2020</dd> <dt>Tags</dt>\n" +
                                "                            <dd>Sample Posts, html example</dd>\n" +
                                "                        </dl>\n" +
                                "                    </footer>\n" +
                                "                </article>\n" +
                                "\n" +
                                "                <form>\n" +
                                "                    <fieldset>\n" +
                                "                        <legend>Personal Information</legend>\n" +
                                "                        <label for=\"name\">Name</label><br>\n" +
                                "                        <input name=\"name\" id=\"name\"><br>\n" +
                                "                        <label for=\"dob\">Date of Birth<label><br>\n" +
                                "                                <input name=\"dob\" id=\"dob\" type=\"date\">\n" +
                                "                    </fieldset>\n" +
                                "                </form>\n" +
                                "\n" +
                                "                <aside>\n" +
                                "                    <p> P inside ASIDE tag </p>\n" +
                                "                </aside>\n" +
                                "                <map name=\"shapesmap\"> <area shape=\"rect\" coords=\"29,32,230,215\" href=\"#square\" alt=\"Square\"> <area shape=\"circle\" coords=\"360,130,100\" href=\"#circle\" alt=\"Circle\"> </map>\n" +
                                "\n" +
                                "                <img src=\"https://placehold.it/100x100\" alt=\"placeholder-image\">\n" +
                                "\n" +
                                "                <form action=\"\" method=\"get\">\n" +
                                "                    <label for=\"browser\">Choose your browser from the list:</label>\n" +
                                "                    <input list=\"browsers\" name=\"browser\" id=\"browser\">\n" +
                                "                    <datalist id=\"browsers\">\n" +
                                "                        <option value=\"Edge\">\n" +
                                "                        <option value=\"Firefox\">\n" +
                                "                        <option value=\"Chrome\">\n" +
                                "                        <option value=\"Opera\">\n" +
                                "                        <option value=\"Safari\">\n" +
                                "                    </datalist>\n" +
                                "                    <input type=\"submit\">\n" +
                                "                </form>\n" +
                                "\n" +
                                "                <footer>\n" +
                                "                    <address> relevant contacts <a href=\"mailto:mail@example.com\">mail</a>.</address>\n" +
                                "                    <div> created by <a href=\"https://blazardsky.space\">@blazardsky</a></div>\n" +
                                "                </footer>\n" +
                                "\n" +
                                "</body>\n" +
                                "\n" +
                                "</html>",
                        "HTML Subj",
                    )
                }
            }
        }
        viewModelScope.launch {
            accountRepository.currentAccount.collect { account ->
                _uiState.update {
                    if (account != null) {
                        it.copy(
                            currentAccount = account.accountId,
                            domain = account.domain,
                            accountStatus = account.status,
                        )
                    } else {
                        it
                    }
                }
                currentAccount = account
            }
        }

        viewModelScope.launch(dispatchers.Main) {
            combine(
                accountRepository.currentAccount,
                contactsRepository.contactsState,
                awalaManager.awalaInitializationState,
                conversationsRepository.conversations,
            ) { currentAccount, contactsState, awalaInitializationState, conversations ->
                logger.d(TAG, "$currentAccount; $contactsState; $awalaInitializationState; ${conversations.size}")
                _uiState.update {
                    it.copy(
                        canSendMessages = contactsState.isPairedContactExist,
                        showTopBarAccountIdAsShimmer = currentAccount?.status == AccountStatus.LINKING_WAITING || currentAccount?.status == AccountStatus.ERROR && !currentAccount.token.isNullOrEmpty(),
                    )
                }
                val rootNavigationScreen = when {
                    awalaInitializationState == AwalaInitializationState.AWALA_NOT_INSTALLED -> RootNavigationScreen.AwalaNotInstalled
                    awalaInitializationState == AwalaInitializationState.INITIALIZATION_NONFATAL_ERROR -> RootNavigationScreen.AwalaInitializationError(type = Route.AwalaInitializationError.TYPE_NON_FATAL_ERROR)
                    awalaInitializationState == AwalaInitializationState.INITIALIZATION_FATAL_ERROR -> RootNavigationScreen.AwalaInitializationError(type = Route.AwalaInitializationError.TYPE_FATAL_ERROR)
                    awalaInitializationState == AwalaInitializationState.COULD_NOT_REGISTER_FIRST_PARTY_ENDPOINT -> RootNavigationScreen.AwalaInitializationError(type = Route.AwalaInitializationError.TYPE_NEED_TO_OPEN_AWALA)
                    awalaInitializationState < AwalaInitializationState.INITIALIZED -> RootNavigationScreen.AwalaInitializing
                    currentAccount == null -> RootNavigationScreen.Registration
                    currentAccount.status == AccountStatus.CREATION_WAITING -> RootNavigationScreen.AccountCreationWaiting
                    currentAccount.status == AccountStatus.LINKING_WAITING -> RootNavigationScreen.AccountLinkingWaiting
                    currentAccount.status == AccountStatus.ERROR -> RootNavigationScreen.AccountCreationFailed
                    !contactsState.isPairRequestWasEverSent -> RootNavigationScreen.WelcomeToLetro(
                        withAnimation = navigationHandledWithLastAccount == currentAccount.id &&
                            (_rootNavigationScreen.value == RootNavigationScreen.AccountCreationWaiting || _rootNavigationScreen.value == RootNavigationScreen.AccountLinkingWaiting || _rootNavigationScreen.value is RootNavigationScreen.WelcomeToLetro),
                    )
                    !contactsState.isPairedContactExist && conversations.isEmpty() -> RootNavigationScreen.NoContactsScreen
                    else -> RootNavigationScreen.Home
                }
                Pair(rootNavigationScreen, navigationHandledWithLastAccount != currentAccount?.id).also { navigationHandledWithLastAccount = currentAccount?.id }
            }
                .debounce(rootNavigationDebounceMs)
                .collect {
                    logger.i(TAG, "New root navigation ${it.first}")
                    val rootNavigationScreen = it.first
                    val lastRootNavigationScreen = _rootNavigationScreen.value
                    this@MainViewModel.rootNavigationScreenAlreadyHandled = true

                    if (rootNavigationScreen != RootNavigationScreen.AwalaNotInstalled && rootNavigationScreen != RootNavigationScreen.AwalaInitializing && rootNavigationScreen !is RootNavigationScreen.AwalaInitializationError) {
                        isAwalaInitialised.emit(true)
                    }

                    _rootNavigationScreen.emit(rootNavigationScreen)

                    val clearNavigationScreenToRoot = it.second
                    if (clearNavigationScreenToRoot && rootNavigationScreen == lastRootNavigationScreen) {
                        logger.d(TAG, "Send event to clear nav stack")
                        _clearBackstackSignal.emit(rootNavigationScreen)
                    }

                    if (isAwalaInitialised.value) {
                        accountIsReadyToProcessActions.emit(true)
                        logger.i(TAG, "Root changed, account is ready to process action")
                    }
                }
        }
    }

    fun onRootNavigationScreenHandled(rootNavigationScreen: RootNavigationScreen) {
        if (rootNavigationScreen == _rootNavigationScreen.value) {
            this.rootNavigationScreenAlreadyHandled = false
        }
    }

    fun onNewAction(action: Action) {
        logger.i(TAG, "ActionHandler: onNewAction ${action.javaClass}")

        viewModelScope.launch(actionProcessorThread) {
            // Do not emit actions while Awala is not initialised, because it cannot be handled
            while (!isAwalaInitialised.value) {
                logger.i(TAG, "ActionHandler: Awala is not initialised yet")
                delay(1_000L)
            }

            logger.i(TAG, "ActionHandler: processing")
            accountIsReadyToProcessActions.emit(false)
            val accountId = action.accountId

            if (accountId != null) {
                val isSwitched = accountRepository.switchAccount(accountId)
                if (!isSwitched) {
                    accountIsReadyToProcessActions.emit(true)
                    logger.i(TAG, "ActionHandler: The same account has to be used to process the action")
                }
            } else {
                accountIsReadyToProcessActions.emit(true)
                logger.i(TAG, "ActionHandler: Account ID is null. Try to process action...")
            }

            // Do not emit action if account was changed. Wait until root screen will be changed (= account was changed)
            while (!accountIsReadyToProcessActions.value) {
                logger.i(TAG, "ActionHandler: Account is not ready to handle actions. Waiting...")
                delay(1_000L)
            }

            logger.i(TAG, "ActionHandler: Sending action ${action.javaClass}")

            withContext(dispatchers.Main) {
                _actions.send(action)
            }
        }
    }

    fun onLinkOpened(link: String) {
        val action = uriToActionConverter.convert(link) ?: return
        onNewAction(action)
    }

    fun onSendFilesRequested(
        files: List<AttachmentToShare>,
        contactId: Long? = null,
    ) {
        onNewAction(
            action = Action.OpenComposeNewMessage(
                attachments = files,
                contactId = contactId,
            ),
        )
    }

    fun onInstallAwalaClick() {
        _openLinkSignal.emitOn(AWALA_GOOGLE_PLAY_LINK, viewModelScope)
    }

    fun onTermsAndConditionsClick() {
        _openLinkSignal.emitOn(termsAndConditionsLink, viewModelScope)
    }

    fun onShareIdClick() {
        currentAccount?.accountId?.let { accountId ->
            _joinMeOnLetroSignal.emitOn(getJoinMeLink(accountId), viewModelScope)
        }
    }

    fun onAttachmentClick(fileId: UUID) {
        viewModelScope.launch(dispatchers.IO) {
            attachmentsRepository.getById(fileId)?.let { attachment ->
                fileConverter.getFile(attachment)?.let { file ->
                    if (file.exists()) {
                        _openFileSignal.emitOn(file, viewModelScope)
                    }
                }
            }
        }
    }

    private fun getJoinMeLink(accountId: String) = "$JOIN_ME_ON_LETRO_COMMON_PART_OF_LINK$accountId"

    companion object {
        const val TAG = "MainViewModel"
        private const val JOIN_ME_ON_LETRO_COMMON_PART_OF_LINK = "https://letro.app/connect/#u="
        private const val AWALA_GOOGLE_PLAY_LINK = "https://play.google.com/store/apps/details?id=tech.relaycorp.gateway"
    }
}

data class MainUiState(
    val currentAccount: String? = null,
    val domain: String? = null,
    @AccountStatus val accountStatus: Int = AccountStatus.CREATED,
    val showTopBarAccountIdAsShimmer: Boolean = false,
    val canSendMessages: Boolean = false,
)
