package com.bc.ceres.binding.swing;

import javax.swing.JComponent;


public abstract class ComponentAdapter {
    private Binding binding;

    public Binding getBinding() {
        return binding;
    }

    public final void setBinding(Binding binding) {
        if (this.binding != null) {
            throw new IllegalStateException("this.binding != null");
        }
        this.binding = binding;
    }

    /**
     * Gets the components participating in the binding.
     *
     * @return The array of components. Must not be empty.
     */
    public abstract JComponent[] getComponents();

    public abstract void bindComponents();

    public abstract void unbindComponents();

    /**
     * Adjusts the bound Swing components in reaction to a property change event in the
     * associated {@code ValueContainer} of the {@link BindingContext}.
     */
    public abstract void adjustComponents();

    /**
     * Handles an error occured while transferring data from the bound property to the
     * Swing component or vice versa.
     * Delegates the call to {@link BindingContext#handleError(Exception, javax.swing.JComponent)}  handleError()}
     * of the binding context using this adapters's first component:
     * <pre>
     * getBinding().getContext().handleError(exception, getComponents()[0]);
     * </pre>
     *
     * @param error The error.
     *
     * @see #getComponents()
     * @deprecated Since 0.10, for error handling use {@link BindingContext#addStateChangeListener(javax.swing.event.ChangeListener)}
     *             and {@link BindingContext#getProblems()} instead
     */
    @Deprecated
    public void handleError(Exception error) {
        getBinding().getContext().handleError(error, getComponents()[0]);
    }
}
