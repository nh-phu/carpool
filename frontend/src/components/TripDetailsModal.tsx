import { useEffect, useState } from 'react'
import { Map, Marker, Source, Layer } from 'react-map-gl'
import 'mapbox-gl/dist/mapbox-gl.css'
import type { LineString } from 'geojson'
import type { Trip } from '../types/Trip'
import type { TripRoute } from '../types/TripRoute'
import './TripDetailsModal.css'

interface TripDetailsModalProps {
  trip: Trip
  onClose: () => void
  actionLabel?: string
  onAction?: () => void
}

export default function TripDetailsModal({
  trip,
  onClose,
  actionLabel = 'Book a seat',
  onAction
}: TripDetailsModalProps) {
  const [route, setRoute] = useState<LineString | null>(null)
  const [routeInfo, setRouteInfo] = useState<TripRoute | null>(null)
  const [routeLoading, setRouteLoading] = useState(true)
  const [routeError, setRouteError] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    async function fetchRoute() {
      setRouteLoading(true)
      setRouteError('')
      setRoute(null)
      setRouteInfo(null)

      try {
        const response = await fetch(
          `/api/trips/${trip.id}/route`,
          { signal: controller.signal },
        )

        if (!response.ok) {
          throw new Error(
            response.status === 404
              ? 'No saved route is available for this trip.'
              : `Failed to load route (${response.status}).`,
          )
        }

        const data: TripRoute = await response.json()

        setRoute(data.geometry)
        setRouteInfo(data)
      } catch (error) {
        if (error instanceof Error && error.name !== 'AbortError') {
          setRouteError(error.message)
        }
      } finally {
        if (!controller.signal.aborted) {
          setRouteLoading(false)
        }
      }
    }

    void fetchRoute()

    return () => controller.abort()
  }, [trip.id])

  const startPosition: [number, number] = [
    trip.startLongitude ?? 105.8542,
    trip.startLatitude ?? 21.0285,
  ]

  const destPosition: [number, number] = [
    trip.destLongitude ?? 105.85,
    trip.destLatitude ?? 21.033,
  ]

  const formatPrice = (price: number) =>
    new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0,
    }).format(price)

  const formatDate = (date: string) =>
    new Date(date).toLocaleString('vi-VN', {
      dateStyle: 'medium',
      timeStyle: 'short',
    })

  function handleBackdropClick(
    event: React.MouseEvent<HTMLDivElement>,
  ) {
    if (event.target === event.currentTarget) {
      onClose()
    }
  }

  return (
    <div
      className="trip-modal-backdrop"
      onMouseDown={handleBackdropClick}
    >
      <section
        className="trip-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="trip-modal-title"
      >
        <header className="trip-modal-header">
          <div>
            <p className="trip-modal-eyebrow">TRIP DETAILS</p>
            <h2 id="trip-modal-title">
              {trip.start} → {trip.dest}
            </h2>
          </div>

          <button
            className="trip-modal-close"
            type="button"
            onClick={onClose}
            aria-label="Close trip details"
          >
            ×
          </button>
        </header>

        <div className="trip-modal-body">
          <div className="trip-route-map">
            <Map
              mapboxAccessToken={import.meta.env.VITE_MAPBOX_TOKEN}
              initialViewState={{
                longitude: startPosition[0],
                latitude: startPosition[1],
                zoom: 12,
              }}
              style={{ width: '100%', height: '100%' }}
              mapStyle="mapbox://styles/mapbox/streets-v12"
              attributionControl={false}
            >
              {/* Start marker */}
              <Marker
                longitude={startPosition[0]}
                latitude={startPosition[1]}
                color="#0d9488"
                anchor="bottom"
              />

              {/* Destination marker */}
              <Marker
                longitude={destPosition[0]}
                latitude={destPosition[1]}
                color="#e76f51"
                anchor="bottom"
              />

              {/* Route line */}
              {route && (
                <Source id="route" type="geojson" data={route}>
                  <Layer
                    id="route-line"
                    type="line"
                    paint={{
                      'line-color': '#0d9488',
                      'line-width': 4,
                      'line-opacity': 0.85,
                    }}
                  />
                </Source>
              )}
            </Map>
          </div>

          {routeLoading && (
            <p className="trip-route-status">Loading route...</p>
          )}

          {routeError && (
            <p className="trip-route-error" role="alert">
              {routeError}
            </p>
          )}

          {routeInfo && (
            <div className="trip-route-summary">
              <div>
                <span className="trip-info-label">Distance</span>
                <strong>
                  {(routeInfo.distance / 1000).toFixed(1)} km
                </strong>
              </div>

              <div>
                <span className="trip-info-label">
                  Estimated driving time
                </span>
                <strong>
                  {Math.round(routeInfo.duration / 60)} min
                </strong>
              </div>
            </div>
          )}

          <div className="trip-info-section">
            <h3>Journey information</h3>

            <div className="trip-info-grid">
              <div className="trip-info-item">
                <span className="trip-info-label">Departure</span>
                <strong>{formatDate(trip.departureTime)}</strong>
              </div>

              <div className="trip-info-item">
                <span className="trip-info-label">Price per seat</span>
                <strong>{formatPrice(trip.priceSeat)}</strong>
              </div>

              <div className="trip-info-item">
                <span className="trip-info-label">Available seats</span>
                <strong>
                  {trip.availableSeats}{' '}
                  {trip.availableSeats === 1 ? 'seat' : 'seats'}
                </strong>
              </div>
            </div>
          </div>

          <div className="trip-info-section">
            <h3>Driver</h3>

            <div className="trip-driver">
              <div className="trip-driver-avatar" aria-hidden="true">
                {trip.driver.name.charAt(0).toUpperCase()}
              </div>

              <div className="trip-driver-details">
                <strong>{trip.driver.name}</strong>
                <span>{trip.driver.phoneNumber || "No phone number provide"}</span>
              </div>

              <span className="trip-driver-badge">Driver</span>
            </div>
          </div>
        </div>

        <footer className="trip-modal-footer">
          <button
            className="trip-modal-secondary"
            type="button"
            onClick={onClose}
          >
            Close
          </button>

          <button
            className="trip-modal-primary"
            type="button"
            onClick={onAction ?? onClose}
          >
            {actionLabel}
          </button>
        </footer>
      </section>
    </div>
  )
}