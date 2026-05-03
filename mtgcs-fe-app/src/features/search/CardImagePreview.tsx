import { useState, useRef } from 'react'
import { Box, CircularProgress, Fade, Paper, Popper } from '@mui/material'

const imageCache = new Map<string, string | null>()

export const fetchScryfallImage = async (cardName: string, setCode?: string | null) => {
  const key = `${cardName}|${setCode ?? ''}`
  if (imageCache.has(key)) return imageCache.get(key)!
  const params = new URLSearchParams({ fuzzy: cardName })
  if (setCode) params.set('set', setCode)
  try {
    const res = await fetch(`https://api.scryfall.com/cards/named?${params}`)
    if (!res.ok) {
      imageCache.set(key, null)
      return null
    }
    const data = await res.json()
    const url: string | null =
      data.image_uris?.normal ?? data.card_faces?.[0]?.image_uris?.normal ?? null
    imageCache.set(key, url)
    return url
  } catch {
    imageCache.set(key, null)
    return null
  }
}

interface Props {
  cardName: string
  setCode?: string | null
  children: React.ReactNode
}

const CardImagePreview = ({ cardName, setCode, children }: Props) => {
  const anchorRef = useRef<HTMLSpanElement>(null)
  const [popOpen, setPopOpen] = useState(false)
  const [imageUrl, setImageUrl] = useState<string | null | undefined>(undefined)

  const handleMouseEnter = async () => {
    setPopOpen(true)
    if (imageUrl !== undefined) return
    const url = await fetchScryfallImage(cardName, setCode)
    setImageUrl(url)
  }

  return (
    <>
      <span
        ref={anchorRef}
        onMouseEnter={handleMouseEnter}
        onMouseLeave={() => setPopOpen(false)}
        style={{ flex: 1, minWidth: 0 }}
      >
        {children}
      </span>
      <Popper
        open={popOpen}
        anchorEl={anchorRef.current}
        placement="right-start"
        transition
        sx={{ zIndex: 1400, pointerEvents: 'none' }}
      >
        {({ TransitionProps }) => (
          <Fade {...TransitionProps} timeout={150}>
            <Paper elevation={8} sx={{ p: 0.5, borderRadius: 2, ml: 1 }}>
              {imageUrl ? (
                <img
                  src={imageUrl}
                  alt={cardName}
                  style={{ width: 220, borderRadius: 8, display: 'block' }}
                />
              ) : (
                <Box
                  sx={{ p: 1.5, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                >
                  <CircularProgress size={20} />
                </Box>
              )}
            </Paper>
          </Fade>
        )}
      </Popper>
    </>
  )
}

export default CardImagePreview
