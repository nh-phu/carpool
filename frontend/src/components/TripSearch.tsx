interface TripSearchProps {
  onSearch: (start: string, dest: string, date: string) => void
}

function TripSearch({ onSearch }: TripSearchProps) {
  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const formData = new FormData(event.currentTarget)

    const start = String(formData.get('start') ?? '').trim()
    const dest = String(formData.get('dest') ?? '').trim()
    const date = String(formData.get('date') ?? '')

    if (!start || !dest) {
      return
    }

    onSearch(start, dest, date)
  }

  return (
    <form id="search" className="search-card" onSubmit={handleSubmit}>
      <label>
        From
        <input
          type="text"
          name="start"
          placeholder="Where are you leaving from?"
          required
        />
      </label>

      <label>
        To
        <input
          type="text"
          name="dest"
          placeholder="Where are you going?"
          required
        />
      </label>

      <label>
        Departure date
        <input type="date" name="date" />
      </label>

      <button type="submit" className="search-button">
        Search rides
      </button>
    </form>
  )
}

export default TripSearch