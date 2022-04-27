package AILib.agents;

import AILib.utils.ArrayUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

public class QAgent extends NeuralNetwork implements Serializable {
    public QAgent(int inputNeurons) {
        super(inputNeurons);
    }

    public QAgent(String fileName) throws IOException, ClassNotFoundException {
        super(fileName);
    }

    public void learningIteration(double reward, double[] nextState, double discountFactor, double ratio){
        double[] nextQValues = this.react(nextState);
        this.setError(reward, nextQValues, discountFactor);

        this.findError();
        this.backWeights(ratio);
    }

    private void setError(double reward, double[] nextQValues, double discountFactor){
        double[] errors = new double[this.layers.get(this.layers.size() - 1).length()];
        Arrays.fill(errors, 0);

        double maxQ = Arrays.stream(nextQValues).summaryStatistics().getMax();
        int maxQIndex = ArrayUtils.getMaxIndex(nextQValues);
        errors[maxQIndex] =
                reward + maxQ * discountFactor - this.layers.get(this.layers.size() - 1).getNeuron(maxQIndex).output;
        this.layers.get(this.layers.size() - 1).setErrors(errors);
    }

    private void findError(){               //Calculates error of all neurons(without output layer)
        for(int i = this.layers.size() - 2; i > 0; i--){
            this.layers.get(i).findErrors(
                    this.layers.get(i + 1).getErrors(),
                    this.layers.get(i + 1).getWeights()
            );
        }
    }

    private void backWeights(double ratio) {    //Changing weights of neurons. Ratio - learning coefficient
        for(int i = 1; i < this.layers.size(); i++)
            this.layers.get(i).trainLayer(this.layers.get(i - 1).getOutputs(), ratio);
    }
}
