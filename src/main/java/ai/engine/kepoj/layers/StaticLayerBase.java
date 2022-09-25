package ai.engine.kepoj.layers;

import ai.engine.kepoj.entities.Neuron;
import ai.engine.kepoj.functions.ActivationFunction;

public abstract class StaticLayerBase implements Layer{
    protected final ActivationFunction aiFunctions;
    protected Neuron[] neurons;

    protected StaticLayerBase(ActivationFunction functions) {
        this.aiFunctions = functions;

    }

    /**
     * Processes data through every neuron
     * @param data input data with the same size as length of previous layer
     * @since 1.1
     */
    @Override
    public void doLayer(double[] data) {
        for(Neuron neuron : this.neurons)
            neuron.excite(data);
    }

    @Override
    public double[] getOutputs() {
        double[] result = new double[this.neurons.length];
        for(int i = 0; i < this.neurons.length; i++)
            result[i] = this.neurons[i].output;

        return result;
    }

    @Override
    public double[] getErrors() {
        double[] result = new double[this.neurons.length];
        for(int i = 0; i < this.neurons.length; i++)
            result[i] = this.neurons[i].error;

        return result;
    }

    @Override
    public double[][] getWeights() {
        double[][] result = new double[this.neurons.length][];
        for(int i = 0; i < this.neurons.length; i++)
            result[i] = this.neurons[i].weights.stream().mapToDouble(Double::doubleValue).toArray();

        return result;
    }

    @Override
    public double[] getBias() {
        double[] result = new double[this.neurons.length];
        for(int i = 0; i < this.neurons.length; i++)
            result[i] = this.neurons[i].bias;

        return result;
    }

    /**
     * Returns count of the neurons on the layer
     * @return layer length
     * @since 1.1
     */
    @Override
    public int length() {
        return this.neurons.length;
    }

    @Override
    public Neuron getNeuron(int index) {
        return this.neurons[index];
    }

    /**
     * Calculates neuron errors based on the next layer's errors and weights between them
     * @param errors errors on the next layer
     * @param weights weights between current and next layers
     * @since 1.1
     */
    @Override
    public void findErrors(double[] errors, double[][] weights) {
        for(int i = 0; i < this.neurons.length; i++) {
            double error = 0;
            for(int j = 0; j < errors.length;j++)
                error+= errors[j] * weights[j][i];

            this.neurons[i].setError(error);
        }
    }

    /** Updates neurons weights based on their errors
     * @param outputs outputs values on the previous layer
     * @param ratio learning decreasing ratio
     * @since 1.1
     */
    @Override
    public void trainLayer(double[] outputs, double ratio) {
        for (Neuron neuron : this.neurons) {
            for (int j = 0; j < neuron.weights.size(); j++)
                neuron.weights.set(j,
                        neuron.weights.get(j) + ratio * outputs[j] * neuron.error
                );
            neuron.bias += ratio * neuron.error;
        }
    }

    @Override
    public void setOutputs(double[] outputs) {
        for(int i = 0; i < this.neurons.length; i++)
            this.neurons[i].output = outputs[i];
    }
}
