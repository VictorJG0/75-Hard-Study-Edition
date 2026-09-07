# 75 Hard (Study Edition)
A rebranded version of th 75 Hard challenge meant for studying. 

## Overview
This app is designed for students to help them either get into good study habits and routines or to help them stay on track with their current study habits. The app will provide a daily checklist of tasks to complete, as well as a progress tracker to monitor your performance over time.

## Features

- Photo gallery to track your daily progress
- Calendar visualized as a progress bar to show how far along you are in the challenge.
- Checklist of daily tasks to complete.
- Progress tracker to monitor your performance over time.

## Prerequisites
- Java 17+

## Building from source
git clone https://github.com/VictorJG0/75-Hard-Study-Edition

cd repo

## Running the app
1. Open terminal and navigate to the folder where the project is located.
2. Compile:
javac -d bin *.java
3. Run:
java -cp bin Main

## Notes

### Day progression
- To progress to the next day, you must complete all tasks for the current day. If you fail to complete any task, you must start over from Day 1.
- The reset to day 1 setting is done IMMEDIATELY after the button is pressed, there is no confirmation prompt.

### Settings
Take time to look through settings. A setting to return a zip folder exists. THIS ALSO DISAPPEARS AFTER YOU PRESS THE CANCEL BUTTON WHEN FAILED AND/OR COMPLETED.

### Noted bugs
- The original 75 Hard version of the app was designed on ubuntu. After switching to arch hyprland, the app has some issues with rendering the window properly. It is still usable however it just looks a bit pixelated. This is due to arch not supporting X11. This is fixable by changing your config file at your own discretion.

- This app is by no means fully tested and is still in development. This was programmed as a 1st year summer project so I could learn java. If you find any bugs, please report them.
