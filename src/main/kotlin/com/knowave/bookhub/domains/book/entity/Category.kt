package com.knowave.bookhub.domains.book.entity

import com.knowave.bookhub.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "categories", comment = "도서 분류. 자기참조로 계층 구조를 표현한다")
class Category(
    name: String,
    parent: Category? = null,
) : BaseEntity() {

    @Column(nullable = false, length = 50, comment = "분류명")
    var name: String = name
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", comment = "상위 분류. 최상위 분류는 null")
    var parent: Category? = parent
        protected set

    @OneToMany(mappedBy = "parent")
    private val _children: MutableList<Category> = mutableListOf()
    val children: List<Category> get() = _children.toList()

    val isRoot: Boolean get() = parent == null

    /** "문학 > 한국소설" 형태의 전체 경로 */
    fun fullPath(): String = parent?.let { "${it.fullPath()} > $name" } ?: name

    /** 분류명을 변경한다 */
    fun changeName(name: String) {
        this.name = name
    }

    /**
     * 자기 자신이거나 `other`의 하위 분류인지 판별한다.
     *
     * 자신부터 조상 체인을 거슬러 올라가며 `other`를 만나는지 확인한다. 프록시와 실제
     * 인스턴스가 `===`로는 달리 보일 수 있으므로 `id`로 비교한다.
     * 이 순회는 `parent` 지연 로딩을 발생시키므로 **영속성 컨텍스트가 살아 있는
     * 트랜잭션 안에서만 호출해야 한다.** `open-in-view: false`이므로 컨트롤러에서
     * 호출하면 `LazyInitializationException`이 발생한다.
     */
    fun isSelfOrDescendantOf(other: Category): Boolean {
        var current: Category? = this
        while (current != null) {
            if (current.id == other.id) return true
            current = current.parent
        }
        return false
    }

    /**
     * 상위 분류를 변경한다. 자기 자신 또는 자신의 하위 분류를 상위로 지정할 수 없다.
     *
     * 호출자가 [isSelfOrDescendantOf]로 먼저 걸러내는 것이 정상 경로이고, 여기의 `require`는
     * 그 검사를 건너뛴 호출을 막는 불변식 가드다. 트랜잭션 제약은 [isSelfOrDescendantOf]와 같다.
     */
    fun changeParent(newParent: Category?) {
        require(newParent == null || !newParent.isSelfOrDescendantOf(this)) {
            "자기 자신이나 하위 분류를 상위로 지정할 수 없습니다"
        }
        this.parent = newParent
    }
}