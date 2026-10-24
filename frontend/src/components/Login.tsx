import { useState } from 'react'
import './Login.css'

export interface AppUser {
  id: number
  name: string
  phoneNumber: string
}

interface LoginPageProps {
  onLogin: (user: AppUser) => void
  onBack: () => void
}

export default function LoginPage({ onLogin, onBack }: LoginPageProps) {
  const [phoneNumber, setPhoneNumber] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await fetch('/api/users')

      if (!response.ok) {
        throw new Error('Could not connect to the server. Try again.')
      }

      const users: AppUser[] = await response.json()
      const normalizedPhone = phoneNumber.trim()

      const user = users.find(
        (item) => item.phoneNumber === normalizedPhone,
      )

      if (!user) {
        setError('No account found with this phone number.')
        return
      }

      onLogin(user)
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Something went wrong.',
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="login-page">
      <button className="login-back" type="button" onClick={onBack}>
        ← Back to rides
      </button>

      <form className="login-card" onSubmit={handleSubmit}>
        <p className="login-eyebrow">WELCOME BACK</p>
        <h1>Log in</h1>
        <p className="login-description">
          Enter your phone number to continue your journey.
        </p>

        <label className="login-label" htmlFor="phoneNumber">
          Phone number
        </label>
        <input
          id="phoneNumber"
          className="login-input"
          type="tel"
          autoComplete="tel"
          placeholder="e.g. 0901234567"
          value={phoneNumber}
          onChange={(event) => setPhoneNumber(event.target.value)}
          required
        />

        {error && (
          <p className="login-error" role="alert">
            {error}
          </p>
        )}

        <button className="login-submit" type="submit" disabled={loading}>
          {loading ? 'Checking...' : 'Continue'}
        </button>

        <p className="login-signup">
          New to carpool?{' '}
          <button type="button" onClick={() => setError('Account creation is coming soon.')}>
            Make an account
          </button>
        </p>
      </form>
    </main>
  )
}
