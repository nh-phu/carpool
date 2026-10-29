import { useState, useEffect } from 'react'
import TripSearch from './components/TripSearch'
import TripDetailsModal from './components/TripDetailsModal'
import BookingModal from './components/BookingModal'
import Login from './components/Login'
import MyAccount from './components/MyAccount'
import type { User } from './components/Login'
import Signup from './components/Signup'
import MakeTrip from './components/MakeTrip'
import type { Trip } from './types/Trip'
import { API_BASE } from './api'
import './App.css'

function App() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [trips, setTrips] = useState<Trip[] | null>(null)
  const [selectedTripId, setSelectedTripId] = useState<number | null>(null)
  const [bookingTrip, setBookingTrip] = useState<Trip | null>(null)

  const [user, setUser] = useState<User | null>(null)
  const [currentPage, setCurrentPage] = useState<
    'home' | 'login' | 'signup' | 'account' | 'make-trip'
  >('home')
  const [profileMenuOpen, setProfileMenuOpen] = useState(false)

    useEffect(() => {
      function handleClickOutside(event: MouseEvent) {
        const target = event.target as HTMLElement

        if (!target.closest('.profile-container')) {
          setProfileMenuOpen(false)
        }
      }

      document.addEventListener('click', handleClickOutside)

      return () => {
        document.removeEventListener('click', handleClickOutside)
      }
    }, [])

  async function handleSearch(
    start: string,
    dest: string,
    date: string,
  ) {
    setLoading(true)
    setError('')
    setTrips(null)

    try {
      const params = new URLSearchParams()
      params.set('start', start)
      params.set('dest', dest)

      if (date) {
        params.set('date', date)
      }

      const response = await fetch(`${API_BASE}/trips/nearby?${params}`)

      if (!response.ok) {
        throw new Error('Failed to search for trips.')
      }

      const data: Trip[] = await response.json()
      setTrips(data)
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Something went wrong.',
      )
    } finally {
      setLoading(false)
    }
  }

  function handleLogin(loggedInUser: User) {
    setUser(loggedInUser)
    setCurrentPage('home')
    setProfileMenuOpen(false)
  }

  function handleLogout() {
    setUser(null)
    setCurrentPage('home')
    setProfileMenuOpen(false)
  }

  return (
    <main className="app">
      <header className="navbar">
        <a
          className="logo"
          href="#"
          onClick={(event) => {
            event.preventDefault()
            setCurrentPage('home')
            setProfileMenuOpen(false)
          }}
        >
          carpool<span>.</span>
        </a>

        <nav className="nav-links">
          <a
            href="#search"
            onClick={(event) => {
              event.preventDefault()
              setCurrentPage('home')
              setProfileMenuOpen(false)
            }}
          >
            Find a ride
          </a>
          <a
            href="#make-trip"
            onClick={(event) => {
              event.preventDefault()

              if (user === null) {
                setCurrentPage('login')
              } else {
                setCurrentPage('make-trip')
              }

              setProfileMenuOpen(false)
            }}
          >
            Offer a ride
          </a>
        </nav>

        <div className="nav-actions">
          {user === null ? (
            <button
              className="login-button"
              type="button"
              onClick={() => {
                setCurrentPage('login')
                setProfileMenuOpen(false)
              }}
            >
              Log in
            </button>
          ) : (
            <div className="profile-container">
              <button
                className="profile-button"
                type="button"
                aria-label="Open profile menu"
                aria-expanded={profileMenuOpen}
                onClick={() => setProfileMenuOpen((open) => !open)}
              >
                {user.name.charAt(0).toUpperCase()}
              </button>

              {profileMenuOpen && (
                <div className="profile-menu">
                  <div className="profile-info">
                    <strong>{user.name}</strong>
                    <span>{user.phoneNumber}</span>
                  </div>

                  <button
                    type="button"
                    onClick={() => {
                      setCurrentPage('account')
                      setProfileMenuOpen(false)
                    }}
                  >
                    My Account
                  </button>

                  <button type="button" onClick={handleLogout}>
                    Log out
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </header>

      {currentPage === 'login' && (
        <Login
          onLogin={handleLogin}
          onBack={() => setCurrentPage('home')}
          onGoToSignup={() => setCurrentPage('signup')}
        />
      )}

      {currentPage === 'signup' && (
        <Signup
          onSignup={handleLogin}
          onBack={() => setCurrentPage('home')}
          onGoToLogin={() => setCurrentPage('login')}
        />
      )}

      {currentPage === 'account' && user !== null && (
        <main className="account-page">
          <button
            className="login-back"
            type="button"
            onClick={() => setCurrentPage('home')}
          >
            ← Back to rides
          </button>

          <MyAccount user={user} />
        </main>
      )}

      {currentPage === 'make-trip' && user !== null && (
        <main className="make-trip-page-wrapper">
          <button
            className="login-back"
            type="button"
            onClick={() => setCurrentPage('home')}
          >
            ← Back to rides
          </button>

          <MakeTrip
            driverId={user.id}
            onTripCreated={() => {
              setCurrentPage('home')
              setTrips(null)
            }}
          />
        </main>
      )}

      {currentPage === 'home' && (
        <>
          <section className="hero" id="search">
            <div className="hero-content">
              <p className="eyebrow">YOUR JOURNEY, SHARED</p>

              <h1>
                Good trips are
                <br />
                better <span>together.</span>
              </h1>

              <p className="hero-description">
                Find people going your way. Share the ride, split the cost,
                and make the journey better.
              </p>
            </div>

            <div className="search-container">
              <TripSearch onSearch={handleSearch} />
            </div>

            {loading && (
              <p className="status-message">Searching for rides...</p>
            )}

            {error && (
              <p className="error-message">{error}</p>
            )}

            {trips !== null && (
              <div className="trip-results">
                <h2>
                  {trips.length === 0
                    ? 'No rides found'
                    : `${trips.length} ride${trips.length === 1 ? '' : 's'} found`}
                </h2>

                {trips.map((trip) => (
                    <article className="trip-card" key={trip.id}>
                      <div className="trip-card-top">
                        <span className="trip-date">
                          <span aria-hidden="true">◷</span>
                          {new Date(trip.departureTime).toLocaleString('en-GB', {
                            weekday: 'short',
                            day: 'numeric',
                            month: 'short',
                            hour: '2-digit',
                            minute: '2-digit',
                          })}
                        </span>

                        <span className="trip-available">
                          {trip.availableSeats > 0
                            ? `${trip.availableSeats} seat${trip.availableSeats === 1 ? '' : 's'} available`
                            : 'Fully booked'}
                        </span>
                      </div>

                      <div className="trip-card-route">
                        <div className="trip-location">
                          <span className="trip-location-dot pickup-dot" />
                          <div>
                            <span className="trip-label">START</span>
                            <strong>{trip.start}</strong>
                          </div>
                        </div>

                        <div className="trip-route-connector" />

                        <div className="trip-location">
                          <span className="trip-location-dot destination-dot" />
                          <div>
                            <span className="trip-label">DESTINATION</span>
                            <strong>{trip.dest}</strong>
                          </div>
                        </div>
                      </div>

                      <div className="trip-card-bottom">
                        <div className="trip-price-block">
                          <strong>{trip.priceSeat.toLocaleString('vi-VN')} VND</strong>
                          <span>per seat</span>
                        </div>

                        <div className="trip-card-actions">
                          <button
                            className="details-button"
                            type="button"
                            onClick={() => setSelectedTripId(trip.id)}
                          >
                            View details
                          </button>

                          <button
                            className="book-button"
                            type="button"
                            disabled={trip.availableSeats < 1}
                            onClick={() => {
                              if (user === null) {
                                setCurrentPage('login')
                              } else {
                                setBookingTrip(trip)
                              }
                            }}
                          >
                            {trip.availableSeats < 1 ? 'Fully booked' : 'Book a seat →'}
                          </button>
                        </div>
                      </div>
                    </article>

                ))}
              </div>
            )}
          </section>

        {selectedTripId !== null && trips !== null && (() => {
          const selectedTrip = trips.find(
            (trip) => trip.id === selectedTripId,
          )

          if (!selectedTrip) return null

          return (
            <TripDetailsModal
              trip={selectedTrip}
              onClose={() => setSelectedTripId(null)}
              actionLabel="Book a seat"
              onAction={() => {
                setSelectedTripId(null)

                if (user === null) {
                  setCurrentPage('login')
                } else {
                  setBookingTrip(selectedTrip)
                }
              }}
            />
          )
        })()}

          {bookingTrip !== null && user !== null && (
            <BookingModal
              trip={bookingTrip}
              passengerId={user.id}
              onClose={() => setBookingTrip(null)}
              onBookingSuccess={(seatsBooked) => {
                setTrips((previousTrips) => {
                  if (previousTrips === null) {
                    return previousTrips
                  }

                  return previousTrips.map((trip) =>
                    trip.id === bookingTrip.id
                      ? {
                          ...trip,
                          availableSeats: Math.max(
                            0,
                            trip.availableSeats - seatsBooked,
                          ),
                        }
                      : trip,
                  )
                })

                setBookingTrip(null)
              }}
            />
          )}

          <section className="features" id="offer">
            <article className="feature">
              <h3>Save on every trip</h3>
              <p>
                Share travel costs with people heading in the same direction.
              </p>
            </article>

            <article className="feature">
              <h3>Travel together</h3>
              <p>
                Turn a long journey into a more enjoyable shared experience.
              </p>
            </article>

            <article className="feature">
              <h3>Simple booking</h3>
              <p>
                Find a ride, check the details, and reserve your seat.
              </p>
            </article>
          </section>
        </>
      )}
    </main>
  )
}

export default App
