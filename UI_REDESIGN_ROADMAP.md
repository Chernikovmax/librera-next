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

## 3. Improve Book Presentation

- Refine the visual hierarchy of cover, title, author, and format metadata.
- Define title behavior, including line limits and handling of long names.
- Separate opening a book from secondary book actions so they do not compete for attention.
- Check card density and grid behavior at different screen widths.

**Outcome:** Books are easier to scan, and their presentation remains readable on narrow screens and with larger text.

## 4. Restructure Settings

- Group settings into understandable areas, such as library, reading, appearance, and app behavior.
- Reassess whether the current drawer is suitable for the full settings experience; consider a dedicated screen as part of the broader redesign.
- Redesign tightly packed file-format controls and small elements for narrow screens.
- Make important settings and controls comfortable to tap.

**Outcome:** Users can scan and find settings instead of navigating a dense, undifferentiated form.

## 5. Establish a Cohesive Visual Language

- Bring the top bar, library, and settings into one coherent visual system.
- Use shared color and typography rules; treat saturated blue as an accent rather than defaulting to large areas of solid color.
- Check contrast, selected states, focus, and feedback for interactions.

**Outcome:** The app feels cohesive while keeping book covers central to the experience.

## 6. Validate Real-World Layouts and States

- Test narrow screens, long translations, RTL, and increased system font sizes.
- Review empty-library, permission, populated-library, and long-title states.
- Check gesture navigation, system insets, tablets, and window resizing.

**Outcome:** The design works beyond the initial screenshot and remains usable across realistic states and devices.

## Suggested Sequence

1. Draft the Library and Settings screen structures, including their important states.
2. Agree on the visual direction using those two screens.
3. Implement and validate one screen at a time in the existing XML/View architecture.

A UI-framework migration is not a prerequisite; consider one only if the settled design reveals a concrete need.
