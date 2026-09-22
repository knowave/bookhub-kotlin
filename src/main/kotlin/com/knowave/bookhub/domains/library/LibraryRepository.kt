package com.knowave.bookhub.domains.library

import com.knowave.bookhub.domains.library.entity.Library
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LibraryRepository : JpaRepository<Library, UUID>