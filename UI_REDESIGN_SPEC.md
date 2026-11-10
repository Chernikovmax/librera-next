# Librera UI Redesign: Screen Proposal

## Design direction

A quiet, book-first reading interface: neutral page surfaces, restrained ink-like text, and one configurable accent used for selection and primary actions. Covers provide the color. Prefer clear hierarchy and comfortable touch targets over decorative chrome. This is a design direction, not a commitment to Material Components or Compose; implement in the existing Android Views/XML architecture unless a concrete limitation emerges.

### Shared rules

- Reserve the accent for selected navigation, links, and primary actions; avoid large saturated bars behind routine content.
- Use a small spacing scale (4, 8, 12, 16, 24dp), consistent card corners, and restrained elevation. Keep touch targets at least 48dp where practical.
- Use readable body text, a clear screen title, and secondary metadata with sufficient contrast. Honor system font scaling.
- Give controls explicit selected, disabled, pressed, and focus states; preserve accessibility labels and content descriptions.
- Keep layouts valid in RTL and at narrow widths; labels must wrap or move, never disappear behind neighboring controls.

## Library

### Information hierarchy

1. A compact app bar: navigation/drawer affordance, current library section, and only the most frequent global actions. Move infrequent actions (including refresh/sort/view choices as appropriate) into a clearly named overflow/menu rather than compressing icons into one row.
2. A distinct, scrollable section selector with readable labels. At narrow widths, allow horizontal scrolling or use a labeled menu; do not ellipsize every tab to an ambiguous fragment. Preserve the user's configured tab order and hidden tabs.
3. The book collection occupies the main surface. Keep list and cover-grid modes, make the active mode apparent, and retain existing search/filter and folder flows.
4. Permission and library setup are contextual states, not permanent decoration over a populated collection.

### States and actions

- **Populated library:** show books immediately. Any permission reminder is compact, dismissible when valid, and does not obscure covers or navigation. Keep refresh/library-folder management reachable.
- **Empty, permission missing:** explain what access is needed and why, with one primary action to grant it. Do not show an empty-state action when permission is already granted.
- **Empty, no folders/books:** offer add-folder and update/rescan actions with a short explanation. Distinguish this from the permission-required state.
- **Loading/error/offline:** use explicit progress or actionable error feedback; avoid replacing the collection with a vague persistent banner.
- **Navigation:** preserve drawer destinations, favorites, folders, recent, bookmarks, and user-configurable tabs. On small screens, prioritize label clarity over fitting all destinations into a fixed-width row.

### Book items

- Cover remains the dominant visual element; keep grid density responsive to available width.
- Show title on up to two lines with end ellipsis, then author on one or two lines; retain format and other metadata only when space permits.
- Make the cover/title area the primary open-book target. Keep favorite and more-actions separate, with 48dp touch targets and accessible names.
- Verify long titles, missing covers, large fonts, and both list/grid modes before settling card height and density.

## Settings

### Structure

Replace the dense, narrow drawer-as-form with a dedicated, full-width settings screen opened from the existing settings entry point. Keep the main library navigation model unchanged. Use a top app bar with a clear “Settings” title and Back navigation; sections scroll in the content area.

Group controls into scannable sections, in this order:

1. **Library** — source folders and file types, scan/update behavior, library presentation.
2. **Reading** — page/display behavior, orientation, reader controls and gestures.
3. **Appearance** — theme, colors, text and interface appearance.
4. **General** — tabs, screen behavior, language and other application-wide options.
5. **About and support** — version, help, licenses and diagnostics, where currently present.

This is a grouping proposal, not a request to remove settings. Map every existing control to a group and preserve its behavior, persistence, defaults, profile handling, and existing dialogs before moving it.

### Controls and states

- Use full-width rows with a visible setting name, concise current value, and a clear affordance when it opens choices. Keep labels out of tiny right-aligned columns.
- Show file-format choices in a responsive two-column grid (one column at very narrow widths); each option has a readable label and a comfortable checkbox target. Do not split format names across lines.
- Keep library-folder management together: folder list, add-folder/add-storage actions, and explicit rescan/update action. Explain actions with short supporting text where needed.
- Separate destructive or reset actions from routine toggles and preserve confirmation behavior.
- Preserve profile selection and profile editing; make their relationship to settings explicit rather than presenting an unexplained letter and “BETA” pill.
- Support long translations, RTL, screen readers, and increased font size without clipping or horizontal overflow.

## Implementation and validation sequence

1. Inventory and map every existing settings control, state, and entry point; confirm empty-library and permission transitions.
2. Implement the library hierarchy and empty/populated states while retaining configurable navigation and existing actions.
3. Improve book-item title hierarchy and responsive grid behavior.
4. Move settings into the proposed grouped screen without dropping or changing existing preferences.
5. Apply shared colors, typography, spacing, states, and touch-target rules consistently.
6. Check narrow and wide screens, long translations, RTL, large fonts, gesture/system insets, permission/empty/populated/error states, and regression of existing library/settings workflows.

## Explicit non-goals

- No UI toolkit migration or new design-system dependency by default.
- No removal of existing settings, tabs, library sources, or reader workflows.
- No assumption that the current permission prompt can be replaced without preserving its migration and system-picker cases.
