package ise.roletagger.categorytreegeneration;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import ise.roletagger.model.CategoryTree;

public class CategoryTreeGenerator {

	private final List<CategoryTree> trees = new CopyOnWriteArrayList<>();

	public CategoryTreeGenerator(final List<String> seeds,final int treeDepth) {
		final ExecutorService executor = Executors.newCachedThreadPool();
		for(String seed:seeds) {
			executor.submit(handle(seed,treeDepth));
		}
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}

	private Runnable handle(String seed, int treeDepth) {
		final Runnable run = () -> {
			System.out.println("Tree generation started for "+seed);
			trees.add(generateTree(seed, treeDepth));
		};
		return run;
	}

	private CategoryTree generateTree(String seed, int treeDepth) {
		final CategoryTree catTree = new CategoryTree(seed, treeDepth);
		final Queue<String> queue = new LinkedBlockingQueue<>();
		queue.add(seed);

		int currentDepth = 0,
				elementsToDepthIncrease = 1, 
				nextElementsToDepthIncrease = 0;


		while(!queue.isEmpty()) {
			final String pop = queue.poll();
			catTree.addSubClass(pop,currentDepth);
			final Set<String> set = FastLookUpSubjectObject.getFastlookUpSubjectObjects().get(pop);
			if(set==null) {
				if (--elementsToDepthIncrease == 0) {
					if (++currentDepth > treeDepth) {
						break;
					}
					elementsToDepthIncrease = nextElementsToDepthIncrease;
					nextElementsToDepthIncrease = 0;
				}
				continue;
			}
			nextElementsToDepthIncrease += set.size();
			if (--elementsToDepthIncrease == 0) {
				if (++currentDepth > treeDepth) {
					break;
				}
				elementsToDepthIncrease = nextElementsToDepthIncrease;
				nextElementsToDepthIncrease = 0;
			}
			queue.addAll(set);
		}
		return catTree;
	}

	public void printTreesToFile() {
		for(CategoryTree ct:trees) {
			ct.writeToFile();
		}
	}
}
