import org.roaringbitmap.RoaringBitmap
// Client-server protocol messages.

object Protocol11 {

  final case object WorkRequest
  final case class GiveFrame(index: BigInt)
  final case class ResultFrame(frame: Option[RoaringBitmap])
}
