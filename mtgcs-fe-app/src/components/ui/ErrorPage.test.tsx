import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import ErrorPage from './ErrorPage'

describe('ErrorPage', () => {
  it('shows the message from the URL query string', () => {
    render(
      <MemoryRouter initialEntries={['/error?message=Discord%20login%20failed']}>
        <ErrorPage />
      </MemoryRouter>,
    )

    expect(screen.getByText('Discord login failed')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /try discord login again/i })).toHaveAttribute(
      'href',
      '/oauth2/authorization/discord',
    )
  })

  it('shows a generic fallback when the URL has no message', () => {
    render(
      <MemoryRouter initialEntries={['/error']}>
        <ErrorPage />
      </MemoryRouter>,
    )

    expect(screen.getByText('Something went wrong. Please try again.')).toBeInTheDocument()
  })
})
