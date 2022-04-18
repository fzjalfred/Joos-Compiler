package backend.asm;

public abstract class NoOpCode extends Code {

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
