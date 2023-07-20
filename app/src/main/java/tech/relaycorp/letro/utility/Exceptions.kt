package tech.relaycorp.letro.utility

class AccountNotFoundException(address: String) : Exception("Account not found for address: $address")

class ContactNotFoundException(address: String) : Exception("Contact not found for address: $address")
