import { useState } from 'react'
import './MakeTrip.css'

interface MakeTripProps {
    driverId: number
  onTripCreated: () => void
}

export default function MakeTrip({ driverId, onTripCreated }: MakeTripProps) {
  const [start, setStart] = useState('')
  const [dest, setDest] = useState('')
  const [departureTime, setDepartureTime] = useState('')
  const [availableSeats, setAvailableSeats] = useState('3')
  const [priceSeat, setPriceSeat] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSuccess('')

    const departure = new Date(departureTime)

    if (departure <= new Date()) {
      setError('Departure time must be in the future.')
      return
    }

    if (!Number.isInteger(Number(availableSeats)) ||
        Number(availableSeats) < 1) {
      setError('Enter at least one available seat.')
      return
    }

    if (!Number.isFinite(Number(priceSeat)) || Number(priceSeat) < 0) {
      setError('Enter a valid price per seat.')
      return
    }

    setLoading(true)

    try {
      const response = await fetch('/api/trips', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          driverId,
          start: start.trim(),
          dest: dest.trim(),
          departureTime,
          availableSeats: Number(availableSeats),
          priceSeat: Number(priceSeat),
        }),
      })

      if (!response.ok) {
        const message = await response.text()
        throw new Error(message || 'Failed to create trip.')
      }

      setSuccess('Your trip has been created.')
      setStart('')
      setDest('')
      setDepartureTime('')
      setAvailableSeats('3')
      setPriceSeat('')
      onTripCreated()
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Something went wrong.',
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="make-trip-page">
      <div className="make-trip-heading">
        <p className="eyebrow">DRIVE TOGETHER</p>
        <h1>Offer a ride</h1>
        <p>Share your journey with people heading your way.</p>
      </div>

      <form className="make-trip-form" onSubmit={handleSubmit}>
        <label>
          Starting location
          <input
            value={start}
            onChange={(event) => setStart(event.target.value)}
            placeholder="e.g. Hanoi"
            required
          />
        </label>

        <label>
          Destination
          <input
            value={dest}
            onChange={(event) => setDest(event.target.value)}
            placeholder="e.g. Hai Phong"
            required
          />
        </label>

        <label>
          Departure date and time
          <input
            type="datetime-local"
            value={departureTime}
            onChange={(event) => setDepartureTime(event.target.value)}
            required
          />
        </label>

        <div className="make-trip-form-row">
          <label>
            Available seats
            <input
              type="number"
              min="1"
              step="1"
              value={availableSeats}
              onChange={(event) => setAvailableSeats(event.target.value)}
              required
            />
          </label>

          <label>
            Price per seat (VND)
            <input
              type="number"
              min="0"
              step="1000"
              value={priceSeat}
              onChange={(event) => setPriceSeat(event.target.value)}
              placeholder="150000"
              required
            />
          </label>
        </div>

        {error && <p className="make-trip-error">{error}</p>}
        {success && <p className="make-trip-success">{success}</p>}

        <button
          className="make-trip-submit"
          type="submit"
          disabled={loading}
        >
          {loading ? 'Creating trip...' : 'Create trip'}
        </button>
      </form>
    </section>
  )
}
