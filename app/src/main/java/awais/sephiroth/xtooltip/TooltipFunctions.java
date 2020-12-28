package awais.sephiroth.xtooltip;

public interface TooltipFunctions {
    void doOnPrepare(final Tooltip tooltip);
    void doOnShown(final Tooltip tooltip);
    default void doOnHidden(final Tooltip tooltip) {}
    default void doOnFailure(final Tooltip tooltip) {}
}
