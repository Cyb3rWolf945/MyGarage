package pt.ipt.dama2026.mygarage.ui.screens.servicelog

/**
 * Represents the four possible states of the unified Service Log dialog.
 * – HIDDEN: no dialog is shown
 * – ADD:    the user tapped the FAB to create a new service log
 * – EDIT:   the user selected "Edit" from the long-press options sheet
 * – VIEW:   the user tapped a service log in the timeline (read-only)
 */
enum class ServiceDialogMode { HIDDEN, ADD, EDIT, VIEW }
