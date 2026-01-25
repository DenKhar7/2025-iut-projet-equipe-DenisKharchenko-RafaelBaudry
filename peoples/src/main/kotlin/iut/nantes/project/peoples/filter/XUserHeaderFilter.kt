package iut.nantes.project.peoples.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

// Filtre pour la verif du header x-user
@Component
class XUserHeaderFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val xUserHeader = request.getHeader("X-User")
        
        if (xUserHeader.isNullOrBlank()) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.writer.write(
                """
                {
                    "timestamp": "${java.time.ZonedDateTime.now()}",
                    "status": 401,
                    "message": "Missing or empty X-User header"
                }
                """.trimIndent()
            )
            return
        }
        
        filterChain.doFilter(request, response)
    }
}
