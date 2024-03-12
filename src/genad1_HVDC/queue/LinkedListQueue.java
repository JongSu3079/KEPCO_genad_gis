package genad1_HVDC.queue;

public class LinkedListQueue<E> implements Queue<E> {
	
	private Node<E> head;
	private Node<E> tail;
	private int size;
	
	public LinkedListQueue() {
		this.head = null;
		this.tail = null;
		this.size = 0;
	}

	@Override
	public boolean offer(E value) {
		Node<E> newNode = new Node<E>(value); 
		
		// 비어있을 경우
		if(size == 0) {
			head = newNode;
		}
		// 그 외의 경우 마지막 노드(tail)의 next가 새 노드를 가리키도록 한다.
		else {
			tail.next = newNode;
		}
		
		// tail이 가리키는 노드를 새 노드로 바꿔준다.
		tail = newNode;
		size++;
		
		return true;
	}

	@Override
	public E poll() {
		
		// 삭제할 요소가 없을 경우 null 반환
		if(size == 0) {
			return null;
		}
		
		// 삭제될 요소의 데이터를 담을 변수
		E element = head.data;
		
		// head 노드의 다음노드
		Node<E> nextNode = head.next;
		
		// head의 모든 데이터를 삭제
		head.data = null;
		head.next = null;
		
		// nextNode를 head로 변경
		head = nextNode;
		size--;
		
		return element;
	}

	@Override
	public E peek() {
		
		// 요소가 없을 경우 null 반환
		if(size == 0) {
			return null;
		}
		
		return head.data;
	}
	
	public int getSize() {
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}

}
