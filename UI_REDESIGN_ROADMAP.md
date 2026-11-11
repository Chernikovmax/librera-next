# Librera UI Redesign Roadmap

## Goal

Make Librera's interface more comfortable, clear, and visually cohesive without treating any particular UI toolkit or design system as the goal. The redesign may revisit screen structure and navigation while preserving the app's core workflows: finding and opening books, managing the library, and configuring the app.

## 1. Define the Direction and Structure

- Identify the primary tasks on the Library and Settings screens.
- Decide which sections should remain readily accessible and which actions are secondary.
- Establish a visual direction suited to a reading app: calm, readable, and centered on book covers rather than utility chrome.
- Define shared principles for color, typography, spacing, icons, component states, and corner shapes.

**Outcome:** An agreed screen structure and visual direction, independent of Material Design or Compose.

## 2. Redesign the Library Home

- Rework the top bar to prioritize frequent actions and move less-used actions into a menu.
- Make section navigation legible on narrow screens; prevent clipped labels and account for longer translations.
- Reconsider the access-permission prompt: present it as a clear empty-library state when appropriate, rather than a persistent banner above the books.
- Preserve list and grid browsing while clarifying how users switch between them.

**Outcome:** The library content is prominent, and the next useful action is clear without competing utility elements.
Current implementation: The empty-library state appears only when the active library has zero visible books, the search filter is blank, and no scan error is displayed. It replaces the permission banner and offers Add folder and Update actions. Verified on the 320×640 clone emulator in both populated (one book; no permission banner) and empty states; the owner emulator profile was not modified.

## 3. Improve Book Presentation

- Refine the visual hierarchy of cover, title, author, and format metadata.
- Define title behavior, including line limits and handling of long names.
- Separate opening a book from secondary book actions so they do not compete for attention.
- Check card density and grid behavior at different screen widths.

**Outcome:** Books are easier to scan, and their presentation remains readable on narrow screens and with larger text.
Current implementation: Grid columns follow the library viewport width and update on resize. List titles use a two-line, 16sp limit with end ellipsis; opening and secondary actions are visually distinct. The library toolbar label now fits at 320dp after removing its duplicate folder-settings shortcut; folder management remains in Settings. Arabic RTL library navigation and actions were checked at 320dp; some Settings strings remain untranslated. Grid/list resizing and long-title handling were verified only in the previously recorded emulator configurations; other Android/device states remain outside this validation.


## 4. Restructure Settings

- Group settings into understandable areas, such as library, reading, appearance, and app behavior.
- Reassess whether the current drawer is suitable for the full settings experience; consider a dedicated screen as part of the broader redesign.
- Redesign tightly packed file-format controls and small elements for narrow screens.
- Make important settings and controls comfortable to tap.

**Outcome:** Users can scan and find settings instead of navigating a dense, undifferentiated form.

Current implementation: Settings opens as a full-screen page with five category cards: Appearance, Reading, Library, Sync, and Advanced. File-format filters are nested under Library. Existing folder, scanning, Google Drive, engine, profile, backup, and accessibility controls remain available inside their categories. Header and system Back return through the hierarchy; category and scroll state survive palette recreation. Content scrolls clear of the floating navigation at 320dp.

## 5. Establish a Cohesive Visual Language

- Bring the top bar, library, and settings into one coherent visual system.
- Use shared color and typography rules; treat saturated blue as an accent rather than defaulting to large areas of solid color.
- Check contrast, selected states, focus, and feedback for interactions.

**Outcome:** The app feels cohesive while keeping book covers central to the experience.

Current implementation: Settings section headings no longer use full-width saturated-blue backgrounds; blue remains on navigation and primary toolbar surfaces. Settings keyboard focus is isolated while the full-screen page is open and returns to the library on close; checked with TAB traversal on the 320×640 emulator. Catalog-entry category text uses a darker teal token (#00695C), with a calculated 6.61:1 contrast ratio against white. Library-list and grid author metadata now use the same dark teal token instead of the brighter system link color; grid author text is bound to the book's actual metadata rather than a hardcoded placeholder. Bookmark-list author metadata uses this token as well. Remove/bookmark icons in library, file-browser, bookmarks, tags and reading controls now use muted gray (#757575), with a calculated 4.61:1 contrast ratio against white. Both list and grid rows were visually verified in the 320×640 emulator; build and install succeeded. Tapping between Library and Favorites on the clone emulator visibly updates the active tab icon/label color; inactive tabs remain muted. Focus was checked in Settings and library focus returns on close. Other selected/focus treatments on individual controls and untested Android states remain limitations, not known defects.
 

## 6. Validate Real-World Layouts and States

- Test narrow screens, long translations, RTL, and increased system font sizes.
- Review empty-library, permission, populated-library, and long-title states.
- Check gesture navigation, system insets, tablets, and window resizing.

**Outcome:** The design works beyond the initial screenshot and remains usable across realistic states and devices.

Current validation: On a 320×640 Android 36 emulator, the library grid/list and settings screen were checked; the list title wraps to two lines. Grid author metadata was verified with the indexed book. Settings were also checked at 1.3× system font scale, where controls wrap and remain scrollable. Arabic RTL layout was checked in the library and settings: navigation, toolbar actions, and the settings header mirror correctly; controls remain within the viewport. Russian library navigation and settings were checked at 320dp: the tab strip scrolls the selected tab fully into view, and the settings folder/format controls fit. Tablet-sized 800×1280 and landscape 1280×800 emulator viewports were checked for library grid/list and settings scrolling; no clipping or interaction regression was observed. On the clone emulator, the empty-library state appeared with zero visible books and was absent when one book was indexed; it contains Add folder and Update actions. Tapping Library and Favorites visibly changes the active navigation icon/label appearance. Settings keyboard focus isolation was verified after two TAB presses, and library focus returns after closing settings. The original owner profile was not changed. A scan-lifecycle defect was fixed: the empty-library call to action now stays hidden while a full library scan is active and returns after the scan completes with no visible books. The updated build was installed on the clone emulator; the zero-book and populated-library states were rechecked. The app uses Android gesture navigation, but dedicated permission-denied, scan-error, gesture-inset edge cases, and per-control focus/selected states were not exhaustively exercised; these remain validation limits, not verified defects.

## Suggested Sequence

1. Draft the Library and Settings screen structures, including their important states.
2. Agree on the visual direction using those two screens.
3. Implement and validate one screen at a time in the existing XML/View architecture.

A UI-framework migration is not a prerequisite; consider one only if the settled design reveals a concrete need.

## Fixed-palette foundation — implementation status

The reference for new work is `ui-redesign/librera-next-task.md` and its interactive HTML mockup. Its Cream, OLED Black, and E-Ink Color palettes supersede the earlier accent-color direction above.

Implemented:

- Semantic background, surface, text, primary, outline, selection, and reader color resources, with three fixed theme overlays and shared text styles.
- `ThemeManager` applies the palette before activity creation. The Appearance palette cards persist the choice using the existing profile storage; system dynamic colors do not override it.
- Settings remain open at their previous scroll position after a palette change. Library navigation and reader activities use the same selected palette. Explicit preset selection also applies its page background/text colors and day/night mode, disabling the active background texture; typography and library contents remain unchanged.
- Header and status-bar foregrounds follow their painted backgrounds, including the translucent floating header.
- E-Ink uses static selectors, flat navigation/cards, and visible card borders instead of decorative elevation. Other palettes retain existing shadow preferences.

This completes the palette integration, not the whole design-system checklist. Library home, bottom navigation, reader controls, and grouped settings are described below; the remaining shared component/icon migration still needs implementation against the mockup.

Validation: debug APK and instrumentation APK built locally. `ThemePaletteContrastTest` passed for all three palettes over both legacy theme parents. A 320×640 Android 36 emulator exercised all three settings palettes, selection/scroll restoration, persistence across process restart, and opening an existing Alice EPUB in the paged MuPDF reader. OLED library header and status icons were visually checked after fixing legacy white foreground overrides. Physical Meebook/Honor devices and PDF rendering were not checked.

Manual smoke: open Settings → Appearance, select each palette card, confirm the category and settings position are retained, restart the app, and open an existing book. Check the matching page colors: Cream #F6EFE3/#352B25, OLED Black #000000/#D4CEC6, and E-Ink Color #FFFFF6/#1C2820 (background/text). Library contents must remain unchanged.

## Library home — implementation status

- Added a compact Librera toolbar with expandable search and the existing sorting/display menus.
- Added a library heading, real book/reading counts, and All / Reading / Read shelf filters. Query/filter misses show an inline empty state rather than the first-import screen.
- Added a Continue Reading card using the most recently opened unfinished book, its cover, author, title, and saved progress. It opens the existing reader and refreshes after returning.
- Added library-specific list/grid card layouts, semantic colors, rounded covers, and flat outlined E-Ink cards. Other library sections retain their existing adapter layouts.
- Kept the existing import, search history, book actions, and metadata grouping paths; no database or reader-engine migration.

Validation: local `assembleLibreraDebug` and `assembleLibreraDebugAndroidTest` builds succeeded. On an isolated 320×640 Android 36 emulator, checked Cream home rendering, empty Read filter, unsuccessful search and clearing it, list/grid switching, shelf scrolling, opening the bundled Alice EPUB, and reopening it through Continue Reading after returning to the library. Fixed an initial header inflation crash before repeating the launch scenario. `ThemePaletteContrastTest` passed (1 test). Physical devices and OLED/E-Ink rendering of these new layouts were not checked in this stage.

## Navigation and reader chrome — implementation status

- Added four floating destinations: Books, Shelves, Favorites, and Settings. Shelves retains the folder/source browser; secondary destinations, including recent books and bookmarks, remain in the existing navigation menu.
- Added inset-aware floating reader toolbars and page progress controls to the paged and vertical readers, using the existing bookmark, page navigation, contents, search, TTS, and advanced-settings actions. Panels auto-hide without resizing the document viewport; accessibility keeps the legacy controls available.
- Added a native Material appearance bottom sheet for reflowable books: font, size, spacing, margins, alignment, paragraph indentation, and hyphenation. Changes remain drafts until Apply; per-book overrides and defaults are stored separately through the existing book/profile persistence.
- Fixed-layout documents retain their original rendering and show an explanatory appearance sheet instead of inappropriate typography controls.
- Corrected the library heading position below its toolbar and restored visible reader icon/slider drawables under legacy activity themes.

Validation: local `assembleLibreraDebug` succeeded. On an isolated 320×640 Android 36 emulator, exercised all four destinations, paged EPUB toolbar/menu/back navigation, seeking to the middle of a book, and font reflow from 1167 to 391 pages while retaining chapter VII and approximately 50% progress. Dismissing an edited appearance sheet retained the saved font size. Opened a two-page PDF, verified the fixed-layout appearance message, and visually checked floating panels in the vertical PDF reader. The final paged-reader screenshot confirms visible icons/seek thumb and no duplicate legacy footer beneath the panel. Installed the debug APK with `adb install -r` on the visible emulator and launched the library without clearing app data. Physical devices, TalkBack, and the new controls in OLED/E-Ink were not exercised in this stage.

## Grouped settings — implementation status

- Replaced the long settings landing form with five localized category cards and a nested file-format page. Existing controls and handlers are retained rather than replaced by placeholder actions.
- Added fixed-palette previews and a defaults-only typography sheet accessible without opening a book. Apply updates profile defaults; Cancel discards the draft, and an active book's override remains separate.
- Preserved category/scroll state during theme changes and nested back navigation. Added system-bar clearance and scrollable bottom space above the floating navigation.
- Kept real Google Drive account/sync controls, with an explanatory message for builds without Google services.

Validation: local `assembleLibreraDebug` succeeded. On an isolated 320×640 Android 36 emulator, exercised all five categories, nested format navigation and scroll restoration, default font-size Apply/reopen and Cancel/reopen, signed-out sync controls, and palette changes while staying in Appearance. Fixed short-page scrolling so Advanced can move fully above the navigation bar. Cloud sign-in/synchronization, permission failures, TalkBack, physical E-Ink devices, and large-text/RTL behavior of the new category cards were not exercised in this stage.

The final debug APK was installed with `adb install -r` on the visible emulator (`emulator-5554`), without clearing application data, and launched directly into the new Settings overview. E-Ink palette previews were visually rechecked after excluding their fixed foreground colors from legacy ink recoloring.

## E-Ink and polish — implementation status

- Removed legacy search-field styling from the library menu button. Toolbar actions now have 8dp edge clearance, 48×48dp touch targets, and round, static pressed/focused states.
- Disabled fast-scroll transitions and recycler item animations in E-Ink mode, used immediate library navigation, and removed E-Ink appearance-sheet/button elevation and window animation.
- Corrected system-bar contrast against the active palette background. Navigation labels remain single-line with full destination descriptions for accessibility.
- Kept the appearance sheet expanded and recalculated its height on configuration changes, preserving scrolling and the Apply action in landscape at 150% system font size.

Validation: local `:app:assembleLibreraDebug` succeeded. On the isolated Android 36 emulator, checked Cream, OLED Black, and E-Ink Color system bars, large-text settings, portrait/landscape appearance-sheet scrolling and Apply visibility, and menu opening. Confirmed toolbar action bounds of 48×48dp with an 8dp horizontal inset. TalkBack bound successfully and visibly focused an app control; complete spoken traversal was not validated. Restored system font scale, rotation settings, and accessibility-service state after the checks.

Installed the final debug APK with `adb install -r` on the visible emulator (`emulator-5554`) and launched the app without clearing its data. Physical E-Ink ghosting, physical OLED display behavior, full TalkBack coverage, legacy settings touch-target cleanup, and a complete document-format regression remain unverified; this does not mark the full device QA checklist complete.

## Document-format regression — emulator results

Ran the following smoke checks on the isolated 320×640 Android 36 emulator. Fixtures were temporary and were not added to the application or the user's emulator.

| Format | Fixture | Observed result |
| --- | --- | --- |
| EPUB | Bundled Alice's Adventures in Wonderland | Rendered, search returned matches for `Alice`, quick bookmark survived reopening, and reopening retained page 2646/5291. |
| FB2 | Generated five-chapter document with repeated searchable paragraphs | Rendered, search returned matches for `comfortable`, saved quick bookmarks remained available, and a controlled close/reopen retained page 629/1263. |
| PDF | Two-page zlib library manual | Rendered both pages, search returned a match for `compression`, page 2 and its quick bookmark survived reopening, and opening from Recent worked. A document hyperlink displayed its URL confirmation; it was cancelled without opening a browser. |
| DJVU | DjVuLibre upstream manual, `doc/djvulibre-book-en.djvu` | Rendered, search returned matches for `DjVu`, and page 29/57 plus its quick bookmark survived reopening. |

- Recent listed all four formats with their reading progress.
- EPUB reflow after an actual portrait/landscape rotation retained 50% progress; page counts changed with the viewport. Switching from E-Ink Color to OLED Black and reopening also retained 50%.
- OLED Black styled the reader chrome without inverting the PDF page. The PDF appearance action explained that typography is unavailable for fixed-layout documents.
- No production-code changes were needed: an initially suspected FB2 reopen drift was not reproduced by the controlled close/reopen check. Earlier observations included navigation taps and are not evidence of a restoration defect.

These are smoke results, not a complete format/device matrix: text selection, DRM, malformed or image-only documents, every renderer/layout combination, rotation/theme changes for every format, physical Meebook/OLED behavior, and full TalkBack traversal remain unverified. The existing large reader font setting was preserved during the checks; pagination values are fixture/profile-specific.

Delivery: local `:app:assembleLibreraDebug` succeeded (70 tasks up-to-date). Installed the debug APK with `adb install -r` on the visible emulator (`emulator-5554`), received `Success`, launched the app, and visually checked the library screen. Application data was not cleared.

## Shared settings sliders — implementation status

- Reworked `custom_seek.xml`: labels wrap above the control row instead of competing with the slider in a fixed 95dp column. Existing label actions and caller IDs remain unchanged.
- Increment/decrement buttons now use 48×48dp targets, 24dp icons, semantic foregrounds, and the existing static round interaction selector. The slider and value row are 48dp high; value calculations and callbacks are unchanged.
- Validation: local `:app:assembleLibreraDebug` succeeded. On the isolated 320dp-wide Android 36 emulator, the cover-size increment changed 80 → 81, decrement restored 80, and dragging the slider changed the value to 142. Checked actual rendering in Cream, OLED Black, and E-Ink Color, at 150% system font size, and in landscape after scrolling the control above floating navigation. System font scale and rotation settings were restored afterward.

This closes the shared `CustomSeek` layout slice, not the remaining legacy settings controls or the full accessibility/device checklist. Full TalkBack traversal and physical E-Ink/OLED behavior were not tested in this slice.

Delivery: installed the debug APK on the visible emulator (`emulator-5554`) with `adb install -r` (`Success`), launched the app, and visually checked the library. Application data was not cleared.

## Cover column controls — implementation status

- Split the cover column selector and Restore default action into separate rows in `fragment_preferences.xml`. The column label can wrap and labels its selector; the reset button fills its own row and allows multiline text. Existing handlers and the reset confirmation are unchanged.
- Validation: local `:app:assembleLibreraDebug` succeeded. On the isolated 320dp-wide emulator at 150% system font size, both controls rendered with full text and 48dp-high touch targets. Selecting 3 columns updated the grid size to 98; cancelling reset preserved 3 columns; confirming reset restored 2 columns, list size 80, and grid size 152. Restored system font scale afterward.
- Installed the debug APK on the visible emulator (`emulator-5554`) with `adb install -r` (`Success`), launched it, and visually checked the library without clearing application data.

This closes only the cover column/reset row. Other legacy settings rows, full TalkBack traversal, and physical-device validation remain outside this slice.

## Appearance selectors — implementation status

- Moved tab placement, interface language, app font size, and cover radius values below their labels in `fragment_preferences.xml`. Values fill the available row width and can wrap; labels reference their selectors for accessibility. Existing menus and persistence handlers are unchanged.
- Reproduced truncated System, Normal, and Bottom - Icons only values on the isolated 320dp-wide emulator at 150% system font size. After the change, all four selectors rendered with full values and 48dp-high targets; visually checked the E-Ink palette. Selected Large (+1), cover radius 2, and Deutsch and observed the updated values/localized interface. Returned the test settings to Normal, radius 0, system language, and Bottom navigation; restored system font scale.
- Local `:app:assembleLibreraDebug` succeeded. Installed the debug APK on the visible emulator (`emulator-5554`) with `adb install -r` (`Success`), launched it, and visually checked the library without clearing application data.

This closes only these four appearance rows. Full TalkBack traversal, other legacy settings rows, and physical-device validation remain outstanding.

## Settings checkbox targets — implementation status

- Added a checkbox theme overlay scoped to `fragment_preferences.xml`, with 48dp minimum width/height, vertical padding, and multiline labels. Other screens and existing checkbox handlers/colors are unchanged.
- Reproduced 32dp checkbox rows before the change. On the isolated 320dp-wide emulator, verified 48dp rows at normal and 150% system font size, a 71dp wrapping appearance label, and 48–96dp format-filter controls. Tapping the newly enlarged lower edge toggled the cover-effect checkbox; restored its original state. Visually checked Appearance and nested Format filters in E-Ink Color and restored system font scale.
- Local `:app:assembleLibreraDebug` succeeded. Installed the debug APK on the visible emulator (`emulator-5554`) with `adb install -r` (`Success`), launched it, and visually checked the Cream library without clearing application data.

## Shared reader dialogs and navigation icons — implementation status

- Updated five main navigation glyphs to 24dp vectors, with mirrored directional arrows. Reader action backgrounds use the shared round focus/press selector; the appearance action is explicitly focusable.
- Normalized 23 additional shared action vectors to 24dp without changing their paths. Enlarged legacy selection, folder, tap-zone, font, and color dialog controls; removed oversized minimum widths and migrated dialog separators/buttons to semantic colors. The advanced reader toolbar scrolls horizontally with labeled 48dp actions.
- Native rounded dialogs use semantic foregrounds, surfaces, controls, and buttons. E-Ink dialogs have a static outline without elevation or entry animation.
- Advanced and Reading Settings place wrapping values below their labels; reading-settings checkboxes use the shared 48dp overlay. Runtime verification caught a missing `layout_height` in the text-alignment label; fixed it and reopened the actual panel successfully.
- Floating popup headers have separate breadcrumb/title rows and 48dp focusable actions. Their surfaces are opaque and palette-aware; E-Ink has no card elevation. Initial and restored popup dimensions are bounded to the screen so the close action cannot be clipped by an oversized saved width.
- On the isolated 320dp Android 36 emulator at 150% font size, opened Advanced Settings, returned through its Preferences breadcrumb, and opened Reading Settings. Verified full titles and 48×48dp close/font-folder/CSS actions. Opened the text-alignment selector and cancelled without changing the value. Visually checked the reading panel and restored system font scale to 1.0.
- Keyboard smoke passed on the installed APK: Menu, Tab through Back / bookmark / Aa, then Enter opened Appearance. The decorative panel container no longer captures focus; auto-hide defers while controls have keyboard focus or touch exploration is active. TalkBack bound successfully, but spoken labels and complete traversal are not certified. Restored the original disabled accessibility configuration.
- Final reader smoke: cold-opened EPUB text, opened the floating controls, applied Appearance, cancelled Appearance with Back, and opened the advanced toolbar. Text remained rendered after applying/cancelling Appearance.
- Delivery: host `:app:assembleLibreraDebug` succeeded; installed on both emulators without clearing application data (`adb install -r`, with `-d` on the visible emulator to replace its higher version code). Launched and visually checked the visible emulator's Cream library. Verified APK v2 signature; version 10.0.29 (7563), Android 7/API 24+, ARM32/ARM64/x86/x86_64, 68,283,756 bytes.
- Local installer: `app/build/outputs/apk/librera/debug/Librera Librera-10.0.29-mupdf-uni.apk`. SHA-256: `ae3db01b2616be712ef3393ca8aab819e4d6b8838d3aa67763fc0ff6df483419`. Delivery uses the local file, not a download service.

## Interactive-reference alignment — implementation status

- Compared the actual library, floating reader, appearance sheet, and settings overview with `ui-redesign/librera-next.html`, including the three reference palettes. Native 48dp controls, readable text sizing, real book data, saved layout preferences, and explicit appearance Apply/cancel semantics take precedence over browser-only dimensions and sample content.
- Reader progress now uses a separate percentage/page-count row above the sage seek track and previous/next actions. The header uses a chevron and horizontal overflow glyph.
- Appearance uses rounded top corners, a handle, an explicit 48dp close action, and separate slider labels/values. Size and line spacing appear before the additional typography controls; the remaining controls stay scrollable.
- Bottom navigation uses outline book/folder/heart/sun glyphs. The settings overview uses its semantic page background and foreground rather than a filled accent toolbar.
- Library view selection sits beside Bookshelf with its sort action. The continue-reading card uses a compact cover/button/progress layout; list rows emphasize title, author, reading percentage, and format while retaining favorite and book-menu actions.
- Runtime proof on Android 36/320dp: checked library list/grid selection, search open/close, reader controls, appearance cancellation preserving font size 20 and subsequent Apply, and settings in Cream/OLED/E-Ink. Corrected search-header binding and the opaque dialog window; the final appearance sheet shows the dimmed reader and rounded top corners.
- Final host `:app:assembleLibreraDebug` passed. Installed the resulting APK on both emulators without clearing data and cold-launched the visible emulator into the Cream library.
- Current installer: `app/build/outputs/apk/librera/debug/Librera Librera-10.0.29-mupdf-uni.apk`, 68,284,018 bytes. SHA-256: `d9a7983764b5d2757b761e6e0552f4d5e9355825706951ff7500f8dcfc65f733`.

Overall: core library, reader chrome/appearance, five-category settings, three palettes, and the shared icon/dialog pass are implemented. Targeted emulator smoke covers EPUB, FB2, PDF, DJVU, library operations, reader appearance, rotation, enlarged text, and keyboard activation of Appearance. Stage 5 is not full acceptance: complete TalkBack traversal, an exhaustive legacy-control/renderer/state audit, and physical Meebook ghosting/OLED low-brightness validation remain unverified. No physical-device behavior is claimed from emulator results.
