import { Box } from '@mui/material'
import { SearchPanel } from '../../features/search'

const SearchPage = () => {
  return (
    <Box sx={{ height: '100%', overflow: 'hidden' }}>
      <SearchPanel />
    </Box>
  )
}

export default SearchPage
