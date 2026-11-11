# Changelog

All notable changes to Librera Reader.

Release builds: https://github.com/foobnix/LibreraReader/releases · [Unreleased APK Direct Download](http://beta.librera.mobi)

## Unreleased

* Add Cream, OLED Black, and E-Ink Color presets to the Appearance sheet, including fixed-layout documents; Apply saves the selection and closing discards it.
* Give the Appearance sheet an accent-colored Apply button with contrasting text in every palette.

* Preserve palette-aware reader slider colors against legacy white tinting and increase inactive-track contrast on the floating reader controls.

* Add an instant blue-light filter toggle and intensity slider to the center-tap reader controls, reusing separate day/night settings without changing screen brightness.

* Add fixed Cream, OLED Black, and E-Ink Color interface palettes with persistent selection.
* Preserve the open settings panel and scroll position when switching palettes.
* Use semantic header foregrounds and contrast-aware status icons; remove decorative elevation from E-Ink navigation and book cards.
* Redesign the library home with reading counts, a Continue Reading card, shelf status filters, expandable search, and palette-aware list/grid book cards.
* Add four floating navigation destinations and inset-aware floating reader controls with page progress and existing advanced actions.
* Add a native reader appearance sheet with separately saved per-book/default typography, draft cancellation, and position-preserving reflow.
* Group Settings into Appearance, Reading, Library, Sync, and Advanced, with nested format filters, palette previews, and default typography editing outside the reader.
* Remove the legacy square menu border, add inset 48dp toolbar buttons with round interaction states, and retain the appearance sheet's Apply button after rotation at large font sizes.
* Disable E-Ink fast-scroll and navigation motion, remove E-Ink sheet/button elevation, and correct system-bar contrast for all three palettes.
* Give shared settings sliders a separate wrapping label, 48dp controls, and palette-aware round increment/decrement buttons.
* Keep cover column selection and Restore default on separate rows so the reset action remains readable at large font sizes.
* Place appearance dropdowns below their labels with full-width, wrapping values for large fonts and long translations.
* Give native settings checkboxes 48dp minimum touch targets and wrapping labels, including nested format filters.
* Stack advanced reader settings labels and values; use wrapping controls and 48dp checkbox targets in reading settings.
* Use semantic native dialog colors and a static, shadow-free E-Ink dialog surface.
* Replace the main reader navigation glyphs with 24dp vector icons and add a keyboard-focusable appearance action.
* Give floating reader dialogs a separate wrapping title row, 48dp header actions, and screen-bounded restored dimensions; enlarge and label reading-settings icon actions.
* Match reader system bars to the active palette background instead of its accent, including transparent gesture navigation, with contrasting system icons.
* Use the book background around short EPUB and other reflowable pages instead of the fixed-layout surround, preventing gray gaps in night mode.
* Normalize 23 additional shared action vectors to 24dp and enlarge legacy selection, font, folder, tap-zone, and color dialog controls.
* Keep floating reader controls visible during keyboard navigation and touch exploration; skip the decorative panel container in accessibility traversal.
* Make the advanced reader toolbar scroll horizontally with labeled 48dp actions on narrow screens.
* Match the reference reader progress layout with separate percentage/page counters and a sage seek track; use a horizontal overflow glyph.
* Give the appearance sheet rounded top corners, a drag handle, a 48dp close action, and compact labeled typography sliders without changing draft/apply behavior.
* Replace filled bottom-navigation glyphs with outline books, shelves, heart, and sun icons; give the settings overview a surface-colored heading.
* Move the library view selector beside Bookshelf, compact the continue-reading card, and simplify list rows to match the interactive reference.
* Keep the reader visible behind the dimmed appearance sheet and clip both sheet surfaces to the rounded top outline.

## 9.6.7

* New design: floating rounded tabs, round buttons, rounded dialogs and menus, new theme colours.
* Redesigned settings, book info and TTS dialogs.
* Library button and one-tap Vertical/Paged switch in the reader.
* BBCode formatting in books.
* Bookmarks and reading position stay on the right page after font changes and sync.
* Favourites use a heart.

## 9.5.7 (2026-09-02)

**Android Auto**

* Librera now appears as a media app in Android Auto
* Browse recent books in the car, with covers, titles and authors
* Selecting a book resumes it at its saved position
* Playback controls work from the car, the lock screen and Bluetooth

**Text-to-speech**

* Uses Android's standard media notification, with cover art, a progress bar and lock screen controls
* Fixed no speech when the system default engine points at an uninstalled package
* Fixed the notification disappearing, or staying stuck on "please wait", after a long time in the background
* Fixed audio focus not being released on pause, which left other apps muted
* Fixed a crash when requesting the notification permission from the TTS controls

**Reading**

* Upgraded the MuPDF engine to 1.28.3 (Librera patches ported from 1.23.7)
* Text selection now works while "crop white space" is enabled, in both book and scroll modes
* Reading direction (RTL) can now be set in scroll mode, not only in book mode
* Annotating is allowed while crop is enabled
* Fixed reflow when toggling "crop white space"
* Book mode: the clock/battery ticker stops when the status bar is hidden
* New "Show progress slider" setting to toggle the seek bar row
* Improved jump history and "Back" arrow visibility when using the slider
* More consistent brightness values when adjusting by scroll
* Fixes for formatted .txt files

**Google Drive sync**

* Much faster: smaller listing requests, parallel downloads and a change check that skips work when nothing changed
* Deleting a book now propagates: it no longer comes back from another device
* Fixed downloads failing into folders that did not exist locally
* Fixed a sync that could stop early on files without a size
* Temporary download files are no longer uploaded to Drive

## 9.4.21 (2026-07-21)

* Fixed text replacement for multiple words
* Updated the UI for changing text and background colors
* Added an alert for permanent file deletion
* Clicking on File Information metadata now navigates to the library
* Added the "iw" translation and fixed other translations
* Fixed the grid view widget
* Fixed the eye reset timer

## 9.4.8 (2026-05-21)

* All-storage access is optional
* Apps can use the system file manager to open individual files
* Fixed choose profile

## 9.3.75 (2026-04-24)

* Fixed contrast and brightness
* Advanced option to enable contrast and brightness for all reading modes
* Fixed Chinese language
* Fixed search in many PDF and EPUB books

## 9.3.63 (2026-03-02)

* Improvements
* Fixes

## 9.3.55 (2026-01-29)

* Fixes
* Librera for macOS, supports PDF, EPUB, FB2, CBZ, CBR (beta.librera.mobi for downloads)

Older releases: https://github.com/foobnix/LibreraReader/releases
