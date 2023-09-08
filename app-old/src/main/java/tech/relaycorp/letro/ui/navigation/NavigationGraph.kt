package tech.relaycorp.letro.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import tech.relaycorp.letro.ui.SplashScreen
import tech.relaycorp.letro.ui.conversations.ConversationsRoute
import tech.relaycorp.letro.ui.conversations.messages.MessagesRoute
import tech.relaycorp.letro.ui.conversations.newMessage.NewMessageRoute
import tech.relaycorp.letro.ui.onboarding.accountCreation.AccountCreationRoute
import tech.relaycorp.letro.ui.onboarding.actionTaking.ActionTakingRoute
import tech.relaycorp.letro.ui.onboarding.actionTaking.ActionTakingScreenUIStateModel
import tech.relaycorp.letro.ui.onboarding.gatewayNotInstalled.GatewayNotInstalledRoute
import tech.relaycorp.letro.ui.onboarding.pair.PairWithPeopleRoute
import tech.relaycorp.letro.ui.onboarding.useExistingAccount.UseExistingAccountRoute
import tech.relaycorp.letro.utility.navigateWithPoppingAllBackStack

@Composable
fun LetroNavHostContainer(
    navController: NavHostController,
    onNavigateToGooglePlay: () -> Unit,
    onGotItClickedAfterPairingRequestSent: () -> Unit,
    paddingValues: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash.name,
        modifier = Modifier.padding(paddingValues),
    ) {
        composable(Route.AccountConfirmation.name) {
            ActionTakingRoute(
                ActionTakingScreenUIStateModel.AccountConfirmation(
                    onPairWithPeople = {
                        navController.navigate(Route.PairWithPeople.name)
                    },
                    onShareId = {
                        // TODO Replace with sharing id functionality
                        navController.navigateWithPoppingAllBackStack(Route.Conversations)
                    },
                ),
            )
        }
        composable(Route.AccountCreation.name) {
            AccountCreationRoute(
                onNavigateToAccountCreationWaitingScreen = {
                    navController.navigate(
                        Route.WaitingForAccountCreation.name,
                    )
                },
                onUseExistingAccount = {
                    navController.navigate(
                        Route.UseExistingAccount.name,
                    ) // TODO Change to correct functionality
                },
            )
        }
        composable(Route.Conversations.name) {
            ConversationsRoute(onChangeConversationsTypeClicked = { /*TODO*/ })
        }
        composable(Route.GatewayNotInstalled.name) {
            GatewayNotInstalledRoute(
                onNavigateToGooglePlay = onNavigateToGooglePlay,
            )
        }
        composable(Route.Messages.name) {
            MessagesRoute(
                onBackClicked = {
                    navController.popBackStack()
                },
                onReplyClicked = {
                    navController.navigate(Route.NewMessage.name)
                },
            )
        }
        composable(Route.NewMessage.name) {
            NewMessageRoute(onBackClicked = {
                navController.popBackStack()
            })
        }
        composable(Route.PairWithPeople.name) {
            PairWithPeopleRoute(
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToPairingRequestSentScreen = {
                    navController.navigate(Route.PairingRequestSent.name)
                },
            )
        }
        composable(Route.PairingRequestSent.name) {
            ActionTakingRoute(
                ActionTakingScreenUIStateModel.PairingRequestSent(
                    onGotItClicked = onGotItClickedAfterPairingRequestSent,
                ),
            )
        }
        composable(Route.Splash.name) {
            SplashScreen()
        }
        composable(Route.UseExistingAccount.name) {
            UseExistingAccountRoute(
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToAccountConfirmationScreen = {
                    navController.navigate(Route.AccountConfirmation.name)
                },
            )
        }
        composable(Route.WaitingForAccountCreation.name) {
            ActionTakingRoute(ActionTakingScreenUIStateModel.Loading)
        }
    }
}
