package backend.asm;

public abstract class NoOpCode {

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
