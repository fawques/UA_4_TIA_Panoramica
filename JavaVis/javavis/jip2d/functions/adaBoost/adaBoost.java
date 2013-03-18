package javavis.jip2d.functions.adaBoost;

import java.io.Serializable;
import java.util.ArrayList;

import javavis.base.JIPException;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;

public class adaBoost implements Serializable {

	private static final long serialVersionUID = -173962357541448551L;
	double []W;
	ArrayList<feature> weakLearners;
	public strongLearner result;
	
	public adaBoost(int numFaces, int numNonFaces, int w, int h, ArrayList<feature>wl)
	{
		weakLearners = wl;
		result = new strongLearner(w, h);
		int tamTrain = numFaces + numNonFaces;
		double auxF = 1.0d/numFaces;
		double auxNF = 1.0d / numNonFaces;
		W = new double [tamTrain];
		for (int count=0; count<tamTrain; count++)
			if (count < numFaces) W[count] = auxF;
			else W[count] = auxNF;
	}
	
	public void adaBoostTrain(ArrayList<trainExample> trainSet, int maxIterations, int numThreads) throws JIPException
	{
		boolean VERBOSE = true;
		ArrayList<parallelTrainer> trainer;

		int tamTrain = trainSet.size();
		int learnerSize;
		int bestLearner = -1;
		feature learner = null;
		trainExample img;
		double bestError, error;

		double Beta, Alfa;
//		float ealfa, emalfa;

		int count, t;
		boolean exit = false;
		
		for (t=0; t<maxIterations && !exit; t++)
		{
			if(VERBOSE) System.out.println("AdaBoost: Iteration "+t);
			learnerSize = weakLearners.size();

			bestLearner = -1;
			bestError = Double.MAX_VALUE;
			//step 1: normalize W
			normalizeVector(W);
			
			//step 2: find best weak learner
			if(numThreads==1)
			{
				for(count=0; count<learnerSize; count++)
				{
//					if(VERBOSE) System.out.println("Training clasificator "+count+" ("+(count*100/learnerSize)+"%)");
					error = weakLearners.get(count).train(trainSet, W);
					if (error < bestError)
					{
						bestError = error;
						bestLearner = count;
					}
				} 
				learner = weakLearners.remove(bestLearner);
				int numFailures = (int)bestError;
				System.out.println("Number of failures: "+numFailures);
				bestError -= numFailures;
			}
			else
			{
				trainer = new ArrayList<adaBoost.parallelTrainer>();
				for (int i=0; i<numThreads; i++)
				{
					trainer.add(new parallelTrainer(i, numThreads, trainSet));
				}
				for (parallelTrainer pt: trainer)
				{
					try {
						pt.runner.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(1);
					}
					if (pt.bestError < bestError)
					{
						bestError = pt.bestError;
						learner = pt.bestLearner;
					}
				}
				int numFailures = (int)bestError;
				System.out.println("Number of failures: "+numFailures);
				bestError -= numFailures;
								weakLearners.remove(learner);
								trainer = null;
			}
			
			//step 3: compute Alfa
			Beta = bestError / (1 - bestError);
			Alfa = Math.log(1/Beta);
//			Alfa = 0.5 * Math.log((1-bestError)/bestError);
			result.addWeak(learner, Alfa);
			if(VERBOSE)
			{
				System.out.println(learner.toString());
				System.out.println("Error: "+(float)bestError+" Alfa: "+Alfa);
			}
			
			//step 4: update W
//			ealfa = (float)Math.exp(Alfa);
//			emalfa = (float)Math.exp(-Alfa);
			for(count=0;count<tamTrain;count++)
			{
				img = trainSet.get(count);
				if(learner.classify(img.integralImage) == img.type) W[count] *= Beta;
//				if(learner.classify(img.integralImage) == img.type) W[cont] *= emalfa;
//				else W[cont] *= ealfa;
			}
			
			//final step: in classic AdaBoost, a test for the strong classifier should be done here to determine if learning process has finished. 
			if(testSet(trainSet)==0) exit = true;
		} //main for
	}
	
	public int testSet(ArrayList<trainExample> tset) throws JIPException
	{
		int ret = 0;
		int errorFace = 0;
		int errorNonFace=0;
		for(trainExample te: tset)
		{
			if(result.classify(te) == 1) 
			{
				if(te.type == 0) errorNonFace++;
			} else
			{
				if(te.type == 1) errorFace++;
			}
		}
		ret = errorFace + errorNonFace;
		System.out.println("Strong classifier errors: "+ret+" ("+errorFace+", "+errorNonFace+")");
		return ret;
	}
	
	private void normalizeVector(double []vector)
	{
		int count;
		int len = vector.length;
		double total = 0;
		
		for(count=0; count<len; count++)
			total += vector[count];
		//for debug
		System.out.print("Norm: "+total);
				for(count=0; count<len; count++)
					vector[count] /= total;
		total = 0;
		for(count=0; count<len; count++)
			total += vector[count];
		System.out.println(" after Norm: "+total);

	}

	public class parallelTrainer implements Runnable
	{
		Thread runner;

		public feature bestLearner;
		public double bestError;
		public ArrayList<trainExample> trainSet;
		int id, dimension;
		
		public parallelTrainer(int id, int total, ArrayList<trainExample> trainSet)
		{
			runner = new Thread(this, "thread"+id);
			this.id = id;
			dimension = total;
			this.bestLearner = null;
			this.bestError = 1;
			this.trainSet = copyInput(trainSet);
			runner.start();
		}
		
		/**
		 * Use to reduce the concurrent access to share memory for trying to minimize possible 
		 * the blocks between threads.
		 * @param trainSet
		 * @return
		 */
		private ArrayList<trainExample> copyInput(ArrayList<trainExample> trainSet)
		{
			ArrayList<trainExample> ret = new ArrayList<trainExample>();
			trainExample aux;
			for(trainExample te: trainSet)
			{
				aux = new trainExample();
				aux.type = te.type;
				aux.integralImage = (JIPBmpFloat)te.integralImage.clone();
				ret.add(aux);
			}
			return ret;
		}
		@Override
		public void run() {
			int count;
			int learnerSize = weakLearners.size();
			int aux = learnerSize / dimension;
			int len = aux * (id+1);
			this.bestError = Double.MAX_VALUE;
			int best=0;
			double error;
			double []W2 = new double[W.length];
			for(count=0;count<W.length;count++)
				W2[count] = W[count];
			
			try {
				for(count=id*aux; count<len; count++)
				{
					error = weakLearners.get(count).train(this.trainSet, W2);
					if (error < bestError)
					{
						bestError = error;
						best = count;
					}
				} 
				this.bestLearner = weakLearners.get(best);
			} catch(JIPException e) 
			{
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
	}
}
