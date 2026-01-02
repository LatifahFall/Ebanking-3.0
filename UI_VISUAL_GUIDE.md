# Premium FinTech UI - Visual Guide

## 🎨 Color Reference

### Primary Banking Blue
```
#0A4FD6 - Main banking blue (buttons, links, primary actions)
#2196F3 - Interactive states
#42A5F5 - Lighter accents
```

### Success & Status
```
#16A34A - Success green (income, positive trends)
#0EA5E9 - Info blue (crypto, accents)
#DC2626 - Error red (errors only)
```

### Neutrals
```
#F5F7FB - Body background
#FFFFFF - Card background
#0F172A - Dark text
#475569 - Secondary text
```

## 🎭 Component Showcase

### Sidebar (Gradient Blue)
- **Background**: Blue gradient (dark to darker)
- **Text**: White (rgba(255,255,255,0.8))
- **Active**: White background (15% opacity) + white left border
- **Hover**: White background (10% opacity)

### Navbar (Search Integrated)
- **Height**: 80px
- **Background**: White with soft shadow
- **Search Bar**: 
  - Width: 100% (max 600px)
  - Height: 48px
  - Border-radius: 12px
  - Focus: Blue border + shadow ring

### Dashboard Cards
- **Border-radius**: 16px
- **Shadow**: Soft (2px-8px blur)
- **Hover**: Lift -4px + enhanced shadow
- **Icon**: 56px rounded container with shadow

### Info Cards
- **Layout**: Icon + Title + Value + Subtitle
- **Title**: 0.875rem uppercase
- **Value**: 2rem bold
- **Icon**: 56px rounded (14px radius)
- **Top Bar**: 4px accent on hover

## 📐 Spacing Scale

```scss
xs:  4px   (0.25rem)
sm:  8px   (0.5rem)
md:  16px  (1rem)
lg:  24px  (1.5rem)
xl:  32px  (2rem)
2xl: 48px  (3rem)
```

## 🔄 Animations

### Fade In (Dashboard)
```scss
duration: 0.6s
easing: cubic-bezier(0.4, 0, 0.2, 1)
from: opacity 0, translateY(20px)
to: opacity 1, translateY(0)
```

### Hover Effects
```scss
Cards: translateY(-4px) + shadow increase
Buttons: translateY(-2px) + background change
Icons: scale(1.1)
```

### Transitions
```scss
All: 0.2s cubic-bezier(0.4, 0, 0.2, 1)
```

## 💎 Shadow System

### Light Theme
```scss
sm:   0 1px 3px 0 rgba(0, 0, 0, 0.06)
md:   0 2px 8px rgba(0, 0, 0, 0.08)
lg:   0 8px 24px rgba(0, 0, 0, 0.12)
card: 0 2px 8px rgba(0, 0, 0, 0.08)
hover: 0 8px 24px rgba(0, 0, 0, 0.12)
```

### Dark Theme
```scss
sm:   0 1px 3px 0 rgba(0, 0, 0, 0.4)
md:   0 2px 8px rgba(0, 0, 0, 0.5)
lg:   0 8px 24px rgba(0, 0, 0, 0.6)
card: 0 2px 8px rgba(0, 0, 0, 0.5)
hover: 0 8px 24px rgba(0, 0, 0, 0.6)
```

## 🎯 Border Radius

```scss
Small:  8px  (buttons, inputs)
Medium: 12px (menu items, small cards)
Large:  16px (main cards, info cards)
XL:     20px (special containers)
2XL:    24px (hero sections)
Circle: 9999px (avatars, badges)
```

## 📱 Responsive Breakpoints

```scss
xs:  375px  (mobile small)
sm:  640px  (mobile)
md:  768px  (tablet)
lg:  1024px (laptop)
xl:  1280px (desktop)
2xl: 1536px (large desktop)
```

## ✨ Key Features

### 1. Gradient Sidebar
- Vertical gradient from #083EAB to #041D54
- White text with 80% opacity (non-active)
- 100% white on hover and active states
- 4px white left border on active item

### 2. Premium Search Bar
- Integrated in navbar (replaces page title)
- Left-aligned with search icon inside
- Placeholder: "Search transactions, accounts..."
- Focus state with blue ring effect

### 3. Soft Shadow Cards
- No borders (removed completely)
- Soft shadows for depth
- Enhanced shadow on hover
- Lift animation (-4px translateY)

### 4. Blue Color Harmony
- Total Balance: #0A4FD6
- Income: #16A34A (green)
- Expenses: #1565C0 (blue, not red!)
- Crypto: #0EA5E9 (accent blue)

### 5. Smooth Animations
- All transitions: 200ms
- Cubic-bezier easing (0.4, 0, 0.2, 1)
- Fade in on page load
- Lift effects on hover

## 🛠️ Usage Examples

### Using Skeleton Loader
```html
<!-- Card skeleton -->
<app-skeleton-loader variant="card" [count]="3"></app-skeleton-loader>

<!-- Text skeleton -->
<app-skeleton-loader variant="text" [count]="5"></app-skeleton-loader>

<!-- Large text (headings) -->
<app-skeleton-loader variant="text-large" width="60%"></app-skeleton-loader>
```

### Info Card Colors
```html
<!-- Banking blue -->
<app-info-card color="#0A4FD6" ...></app-info-card>

<!-- Success green -->
<app-info-card color="#16A34A" ...></app-info-card>

<!-- Accent blue -->
<app-info-card color="#0EA5E9" ...></app-info-card>
```

### Animation Classes
```html
<!-- Fade in -->
<div class="fade-in">Content</div>

<!-- Slide up -->
<div class="slide-up">Content</div>

<!-- Hover lift -->
<div class="hover-lift">Card</div>
```

## 🎓 Design Principles

1. **Minimalism**: Clean, uncluttered layouts
2. **Consistency**: Same shadows, radius, spacing throughout
3. **Hierarchy**: Clear visual importance (2rem values, 0.875rem labels)
4. **Professionalism**: Banking-grade appearance
5. **Accessibility**: WCAG AAA contrast, touch targets 44px+
6. **Performance**: CSS-only animations, optimized shadows

## 🌐 Browser Support

- Chrome/Edge: Full support
- Firefox: Full support
- Safari: Full support (with vendor prefixes)
- Mobile browsers: Optimized with responsive breakpoints

## 📊 Performance Metrics

- Initial bundle: ~376 KB
- Styles: ~16.5 KB
- Zero runtime JS for animations
- GPU-accelerated transforms
- Optimized shadow rendering

---

**For developers**: All colors use CSS custom properties for easy theming. Update `light-theme.scss` or `dark-theme.scss` to customize.

**For designers**: Figma/Sketch files can be created matching these exact specifications for design handoff.
