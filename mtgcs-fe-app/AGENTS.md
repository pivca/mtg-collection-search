## 🎯 Project Overview

- These instructions apply to all files under this directory.

- **Tech Stack**: React 19, TypeScript, Vite, Tailwind CSS, Zustand, Material-UI.
- **Goal**: High-performance, accessible dashboard for card collection search.

## 📂 Project Structure

- `src/components/ui`: MaterialUi or primitive components.
- `src/features/[feature-name]`: Logic, components, and hooks grouped by feature.
- `src/store`: Zustand store definitions.
- `src/hooks`: Shared custom React hooks.

## 🛠 Commands

- **Install**: `npm install`
- **Dev**: `npm run dev`
- **Test**: `npm test` (Uses Vitest)
- **Lint**: `npm run lint` (ESLint + Prettier)

## 📜 Coding Standards

- **Components**: Always use Functional Components with Arrow Syntax.
- **State**: Prefer `Zustand` for global state and `useState` for local UI state.
- **Data Fetching**: Use TanStack Query for all API interactions.
- **Naming**: Use PascalCase for components and camelCase for hooks/functions.

## 🚀 Workflow: Adding a New Feature

1. Create directory in `src/features/`.
2. Define types in `types.ts`.
3. Create the main component and export from an `index.ts` barrel file.
4. Run `npm test` before declaring the task complete.
