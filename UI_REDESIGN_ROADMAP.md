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
Current implementation: The permission prompt is shown only when the live indexed library count is zero, avoiding a stale profile book-count flag that could display the prompt above existing books. Verified on the 320×640 emulator with one indexed book: no permission prompt appears. Empty-library prompt behavior and its visual design remain unvalidated; the fresh Android user profile also showed the indexed book, so it did not provide an empty-library state.

## 3. Improve Book Presentation

- Refine the visual hierarchy of cover, title, author, and format metadata.
- Define title behavior, including line limits and handling of long names.
- Separate opening a book from secondary book actions so they do not compete for attention.
- Check card density and grid behavior at different screen widths.

**Outcome:** Books are easier to scan, and their presentation remains readable on narrow screens and with larger text.
Current implementation: Grid columns follow the library viewport width and update on resize. List titles use a two-line, 16sp limit with end ellipsis; opening and secondary actions are visually distinct. The library toolbar label now fits at 320dp after removing its duplicate folder-settings shortcut; folder management remains in Settings. Arabic RTL library navigation and actions were checked at 320dp; some Settings strings remain untranslated. Broader device/state validation remains open.


## 4. Restructure Settings

- Group settings into understandable areas, such as library, reading, appearance, and app behavior.
- Reassess whether the current drawer is suitable for the full settings experience; consider a dedicated screen as part of the broader redesign.
- Redesign tightly packed file-format controls and small elements for narrow screens.
- Make important settings and controls comfortable to tap.

**Outcome:** Users can scan and find settings instead of navigating a dense, undifferentiated form.

Current implementation: Settings opens as a full-screen page, with a localized title and accessible close action. Library folders and supported formats are separated; the form has distinct book-cover, general, appearance, reading, app-behavior, tabs, widget, and import/export sections. Folder controls and the format grid fit at 320dp; section headings now use theme-aware text rather than full-width blue panels. At 1.3× system font scale, controls wrap without clipping and remain scrollable; other settings states remain unvalidated.

## 5. Establish a Cohesive Visual Language

- Bring the top bar, library, and settings into one coherent visual system.
- Use shared color and typography rules; treat saturated blue as an accent rather than defaulting to large areas of solid color.
- Check contrast, selected states, focus, and feedback for interactions.

**Outcome:** The app feels cohesive while keeping book covers central to the experience.

Current implementation: Settings section headings no longer use full-width saturated-blue backgrounds; blue remains on navigation and primary toolbar surfaces. Settings keyboard focus is isolated while the full-screen page is open and returns to the library on close; checked with TAB traversal on the 320×640 emulator. Catalog-entry category text uses a darker teal token (#00695C), with a calculated 6.61:1 contrast ratio against white. Library-list and grid author metadata now use the same dark teal token instead of the brighter system link color; the list row was visually verified in the 320×640 emulator, while grid-mode display verification remains pending because the emulator has one book and the grid control did not expose a usable switch. Bookmark-list author metadata now uses this token as well, extending consistent contrast to saved books; build and install succeeded. Broader contrast, selected/focus feedback, and consistency across remaining surfaces still need review.
 

## 6. Validate Real-World Layouts and States

- Test narrow screens, long translations, RTL, and increased system font sizes.
- Review empty-library, permission, populated-library, and long-title states.
- Check gesture navigation, system insets, tablets, and window resizing.

**Outcome:** The design works beyond the initial screenshot and remains usable across realistic states and devices.

Current validation: On a 320×640 Android 36 emulator, the library grid/list and settings screen were checked; the list title wraps to two lines. Settings were also checked at 1.3× system font scale, where controls wrap and remain scrollable. Arabic RTL layout was checked in the library and settings: navigation, toolbar actions, and the settings header mirror correctly; controls remain within the viewport. Russian library navigation and settings were checked at 320dp: the tab strip scrolls the selected tab fully into view, and the settings folder/format controls fit. Tablet-sized 800×1280 and landscape 1280×800 emulator viewports were checked for library grid/list and settings scrolling; no clipping or interaction regression was observed. The updated app was installed and launched; a populated one-book library shows no permission prompt. Settings keyboard focus isolation was verified after two TAB presses, and library focus returns after closing settings. The empty-library prompt remains unvalidated: a fresh Android user profile was created to avoid modifying the populated owner profile, but the emulator displayed a black screen after switching users, preventing inspection.

## Suggested Sequence

1. Draft the Library and Settings screen structures, including their important states.
2. Agree on the visual direction using those two screens.
3. Implement and validate one screen at a time in the existing XML/View architecture.

A UI-framework migration is not a prerequisite; consider one only if the settled design reveals a concrete need.
