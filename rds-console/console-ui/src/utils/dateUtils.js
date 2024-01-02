export function formatTimestamp(timestamp) {
  let date = new Date(timestamp * 1000)
  return date.getFullYear() + ':' + date.getMonth() + ':' + date.getDay() + ' ' + date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds()
}
