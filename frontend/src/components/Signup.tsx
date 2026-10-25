import { useState } from 'react'
import './Signup.css'
import type { User } from './Login'

interface SignupProps {
  onSignup: (user: User) => void
  onBack: () => void
  onGoToLogin: () => void
}

export default function Signup({
  onSignup,
  onBack,
  onGoToLogin,
}: SignupProps) {
  const [name, setName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')


  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await fetch('/api/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: name.trim(),
          phoneNumber: phoneNumber.trim(),
        }),
      })

      if (!response.ok) {
        if (response.status === 409) {
          throw new Error('This phone number is already registered.')
        }

        if (response.status === 400) {
          throw new Error('Please check your name and phone number.')
        }

        throw new Error('Could not create your account. Try again.')
      }

      const user: User = await response.json()
      onSignup(user)
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Something went wrong.',
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="signup-page">
     <button className="signup-back" type="button" onClick={onBack}>
        ← Back to rides
      </button>

      <form className="signup-card" onSubmit={handleSubmit}>
        <p className="signup-eyebrow">START YOUR JOURNEY</p>
        <h1>Create an account</h1>
        <p className="signup-description">
          Join carpool and find people going your way.
        </p>

        <label className="signup-label" htmlFor="name">
          Full name
        </label>
        <input
          id="name"
          className="signup-input"
          type="text"
          autoComplete="name"
          value={name}
          onChange={(event) => setName(event.target.value)}
          required
        />

        <label className="signup-label" htmlFor="signup-phone">
          Phone number
        </label>
        <input
          id="signup-phone"
          className="signup-input"
          type="tel"
          autoComplete="tel"
          placeholder="e.g. 0901234567"
          value={phoneNumber}
          onChange={(event) => setPhoneNumber(event.target.value)}
          required
        />

        {error && (
          <p className="signup-error" role="alert">
            {error}
          </p>
        )}

        <button className="signup-submit" type="submit" disabled={loading}>
          {loading ? 'Creating account...' : 'Sign up'}
        </button>

        <p className="signup-login">
          Already have an account?{' '}
          <button type="button" onClick={onGoToLogin}>
            Log in
          </button>
        </p>
      </form>
    </main>
  )
}
