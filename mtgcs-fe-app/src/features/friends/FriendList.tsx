import {
  List,
  ListItemButton,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Typography,
  Skeleton,
  Box,
} from '@mui/material'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import { useFriends } from '../../hooks/useFriends'
import { useFriendsStore } from '../../store/friendsStore'
import type { Friend } from '../../types'

interface Props {
  onEdit: (friend: Friend) => void
}

const FriendList = ({ onEdit }: Props) => {
  const { data: friends, isLoading } = useFriends()
  const { selectedFriendId, setSelectedFriend } = useFriendsStore()
  const { remove } = useFriends()

  if (isLoading) {
    return (
      <Box className="p-2 flex flex-col gap-2">
        {[1, 2, 3].map((i) => (
          <Skeleton key={i} variant="rectangular" height={52} />
        ))}
      </Box>
    )
  }

  if (!friends?.length) {
    return (
      <Typography variant="body2" color="text.secondary" className="p-4">
        No friends yet. Add one to get started.
      </Typography>
    )
  }

  return (
    <List disablePadding>
      {friends.map((friend) => (
        <ListItemButton
          key={friend.id}
          selected={friend.id === selectedFriendId}
          onClick={() => setSelectedFriend(friend.id)}
        >
          <ListItemText
            primary={friend.displayName}
            secondary={friend.discordId ? `Discord: ${friend.discordId}` : undefined}
          />
          <ListItemSecondaryAction>
            <IconButton
              size="small"
              aria-label={`edit ${friend.displayName}`}
              onClick={(e) => {
                e.stopPropagation()
                onEdit(friend)
              }}
            >
              <EditIcon fontSize="small" />
            </IconButton>
            <IconButton
              size="small"
              aria-label={`delete ${friend.displayName}`}
              onClick={(e) => {
                e.stopPropagation()
                remove.mutate(friend.id)
              }}
            >
              <DeleteIcon fontSize="small" />
            </IconButton>
          </ListItemSecondaryAction>
        </ListItemButton>
      ))}
    </List>
  )
}

export default FriendList
