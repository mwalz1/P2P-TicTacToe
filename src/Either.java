import java.util.Optional;
import java.util.function.Consumer;

public class Either<L, R> {
  /**
   * Constructs a Right.
   *
   * @param right The value.
   * @param <L>   Type of left value.
   * @param <R>   Type of right value.
   * @return A new {@code Right} instance.
   */
  public static <L, R> Either<L, R> right(R right) {
    return new Either<>(null, right);
  }

  /**
   * Constructs a Left.
   *
   * @param left The value.
   * @param <L>  Type of left value.
   * @param <R>  Type of right value.
   * @return A new {@code Left} instance.
   */
  public static <L, R> Either<L, R> left(L left) {
    return new Either<>(left, null);
  }

  final private Optional<L> left;
  final private Optional<R> right;

  // TODO Can be null, put that in comments
  private Either(L left, R right) {
    this.left = Optional.ofNullable(left);
    this.right = Optional.ofNullable(right);
  }

  public void ifRight(Consumer<? super R> consumer) {
    this.right.ifPresent(consumer);
  }

  public void ifLeft(Consumer<? super L> consumer) {
    this.left.ifPresent(consumer);
  }
}