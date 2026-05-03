import { createTheme } from '@mui/material/styles'

// Discord color palette
const discord = {
  // Backgrounds
  bg100: '#1E1F22', // darkest — sidebar / nav
  bg200: '#2B2D31', // mid — sidebar panels
  bg300: '#313338', // main chat / content area
  bg400: '#383A40', // elevated surfaces / cards

  // Brand
  blurple: '#5865F2',
  blurpleHover: '#4752C4',
  blurpleActive: '#3C45A5',

  // Text
  textNormal: '#DBDEE1',
  textMuted: '#80848E',
  textLink: '#00AFF4',

  // Status
  green: '#23A55A',
  red: '#F23F43',
  yellow: '#F0B232',

  // Interactive
  interactiveNormal: '#B5BAC1',
  interactiveHover: '#DBDEE1',
  interactiveMuted: '#4E5058',

  // Borders / dividers
  border: '#3F4147',
}

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: discord.blurple,
      dark: discord.blurpleHover,
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: discord.interactiveNormal,
    },
    background: {
      default: discord.bg300,
      paper: discord.bg400,
    },
    text: {
      primary: discord.textNormal,
      secondary: discord.textMuted,
    },
    divider: discord.border,
    error: { main: discord.red },
    success: { main: discord.green },
    warning: { main: discord.yellow },
  },
  typography: {
    fontFamily: '"gg sans", "Noto Sans", "Helvetica Neue", Helvetica, Arial, sans-serif',
    h6: { fontWeight: 700, fontSize: '1rem' },
    subtitle1: {
      fontWeight: 600,
      fontSize: '0.75rem',
      textTransform: 'uppercase',
      letterSpacing: '0.04em',
    },
    body2: { fontSize: '0.875rem', color: discord.textMuted },
  },
  shape: { borderRadius: 8 },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          backgroundColor: discord.bg300,
          color: discord.textNormal,
          scrollbarWidth: 'thin',
          scrollbarColor: `${discord.interactiveMuted} transparent`,
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: discord.bg100,
          backgroundImage: 'none',
          boxShadow: 'none',
          borderBottom: `1px solid ${discord.border}`,
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: { backgroundColor: discord.bg200, borderRight: `1px solid ${discord.border}` },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 500,
          borderRadius: 4,
          '&.MuiButton-containedPrimary': {
            backgroundColor: discord.blurple,
            '&:hover': { backgroundColor: discord.blurpleHover },
            '&:active': { backgroundColor: discord.blurpleActive },
          },
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 4,
          marginInline: 8,
          paddingBlock: 6,
          '&:hover': { backgroundColor: discord.bg400 },
          '&.Mui-selected': {
            backgroundColor: discord.bg400,
            '&:hover': { backgroundColor: discord.bg400 },
          },
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          backgroundColor: discord.bg100,
          '& fieldset': { borderColor: discord.border },
          '&:hover fieldset': { borderColor: discord.interactiveNormal },
          '&.Mui-focused fieldset': { borderColor: discord.blurple },
        },
        input: { color: discord.textNormal },
      },
    },
    MuiInputLabel: {
      styleOverrides: {
        root: {
          color: discord.textMuted,
          '&.Mui-focused': { color: discord.blurple },
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        icon: { color: discord.textMuted },
      },
    },
    MuiMenu: {
      styleOverrides: {
        paper: { backgroundColor: discord.bg100, border: `1px solid ${discord.border}` },
      },
    },
    MuiMenuItem: {
      styleOverrides: {
        root: {
          '&:hover': { backgroundColor: discord.blurple },
          '&.Mui-selected': { backgroundColor: discord.bg400 },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { borderRadius: 4, fontWeight: 600, fontSize: '0.7rem' },
      },
    },
    MuiDivider: {
      styleOverrides: {
        root: { borderColor: discord.border },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: { backgroundColor: discord.bg200, backgroundImage: 'none' },
      },
    },
    MuiDialogTitle: {
      styleOverrides: {
        root: { color: discord.textNormal, fontWeight: 700, fontSize: '1.25rem' },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-root': {
            backgroundColor: discord.bg200,
            color: discord.textMuted,
            fontWeight: 600,
            fontSize: '0.75rem',
            textTransform: 'uppercase',
            letterSpacing: '0.04em',
            borderBottom: `1px solid ${discord.border}`,
          },
        },
      },
    },
    MuiTableBody: {
      styleOverrides: {
        root: {
          '& .MuiTableRow-root:hover': { backgroundColor: discord.bg400 },
          '& .MuiTableCell-root': { borderBottom: `1px solid ${discord.border}` },
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          color: discord.textMuted,
          '&:hover': { color: discord.textNormal, backgroundColor: discord.bg400 },
        },
      },
    },
    MuiAvatar: {
      styleOverrides: {
        root: { backgroundColor: discord.blurple },
      },
    },
  },
})

export default theme
