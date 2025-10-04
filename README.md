## Todo App (Jetpack Compose)

This is a small Todo app I built for CPSC 411A using Kotlin and Jetpack Compose.  
It shows how to use data classes, state, remember/rememberSaveable, and state hoisting.


## What the app does

- Add tasks with a text field + Add button
- Active tasks show label, checkbox, and a delete (X) button
- Checking a task moves it to Completed
- Unchecking moves it back to Active
- Delete removes the task from the list
- Section headers (“Items” / “Completed Items”) only show if there are tasks in that section
- If a section is empty, a friendly message is shown instead
- Input is trimmed and blank input shows an inline warning
- List and input survive rotation using `rememberSaveable`


## Screenshots

[Active](screenshots/Items(NotDone).png)
[Completed](screenshots/CompletedItems(Done).png)


## Concepts used in this project

- Data class: `TodoItem` models each row
- State: `mutableStateListOf` for the list, `mutableStateOf` for text
- remember/rememberSaveable: keeps data when rotating the screen
- State hoisting: `TodoInputRow`, `TodoList`, `TodoRow` don’t hold state, they get state + callbacks from the parent
- Layout: built with `Row`, `Column`, `TextField`, `Button`, `Checkbox`, and `IconButton`
- Clean flow: parent holds state, children just show UI and send events back up


## How to run

Open in Android Studio, install Android 15 (API 35) SDK, and run on an emulator or a real device.
