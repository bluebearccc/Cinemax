package com.bluebear.cinemax.controller.admin;

import com.bluebear.cinemax.dto.ActorDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.service.admins.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/actors")
public class ActorController {

    @Autowired
    private ActorService actorService;

    @Autowired
    private MovieService movieService;

    // SỬA: Sử dụng cấu hình từ application.properties
    @Value("${app.upload.dir}")
    private String uploadDir;

    // ==================== PHƯƠNG THỨC UPLOAD ẢNH ACTOR ====================

    /**
     * Lấy đường dẫn upload ĐỒNG BỘ với WebConfig
     */
    private String getUploadPath() {
        // ĐỒNG BỘ CHÍNH XÁC với WebConfig: Paths.get(uploadDir).toFile().getAbsolutePath()
        String uploadPath = Paths.get(uploadDir).toFile().getAbsolutePath();

        System.out.println("=== UPLOAD PATH DEBUG ===");
        System.out.println("Config uploadDir: " + uploadDir);
        System.out.println("Resolved upload path: " + uploadPath);
        System.out.println("Directory exists: " + new File(uploadPath).exists());
        System.out.println("Can write: " + new File(uploadPath).canWrite());
        System.out.println("========================");

        // Tạo thư mục nếu chưa tồn tại
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("Created directory: " + created);
            if (!created) {
                System.err.println("FAILED to create upload directory: " + uploadPath);
            }
        }

        return uploadPath;
    }


    /**
     * Upload ảnh actor - ĐỒNG BỘ với WebConfig
     */
    private String uploadActorImage(MultipartFile file, Integer actorId) throws IOException {
        System.out.println("=== UPLOAD ACTOR IMAGE ===");
        System.out.println("File: " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("Actor ID: " + actorId);

        if (file == null || file.isEmpty()) {
            System.out.println("File is null or empty");
            return null;
        }

        // Validate file
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("Invalid filename");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!fileExtension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IOException("Only image files are allowed (jpg, jpeg, png, gif, webp)");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IOException("File size cannot exceed 5MB");
        }

        // SỬA: Sử dụng getUploadPath() đồng bộ với WebConfig
        String uploadPath = getUploadPath();
        File uploadDirectory = new File(uploadPath);

        System.out.println("Upload directory: " + uploadDirectory.getAbsolutePath());
        System.out.println("Directory exists: " + uploadDirectory.exists());
        System.out.println("Can write: " + uploadDirectory.canWrite());

        // Đảm bảo thư mục tồn tại và có quyền ghi
        if (!uploadDirectory.exists()) {
            boolean created = uploadDirectory.mkdirs();
            if (!created) {
                throw new IOException("Cannot create upload directory: " + uploadDirectory.getAbsolutePath());
            }
        }

        if (!uploadDirectory.canWrite()) {
            throw new IOException("No write permission to upload directory: " + uploadDirectory.getAbsolutePath());
        }

        // Xóa ảnh cũ trước khi upload ảnh mới
        deleteOldActorImage(actorId);

        // Tạo tên file mới theo format: actor_{actorId}.{extension}
        String newFilename = "actor_" + actorId + fileExtension;
        File targetFile = new File(uploadDirectory, newFilename);

        System.out.println("Target file: " + targetFile.getAbsolutePath());

        try {
            // SỬA: Sử dụng transferTo thay vì Files.copy
            file.transferTo(targetFile);

            // Kiểm tra file đã được tạo thành công
            if (!targetFile.exists()) {
                throw new IOException("File was not saved successfully");
            }

            System.out.println("File size after save: " + targetFile.length() + " bytes");

            // Trả về web path (khớp với WebConfig mapping /uploads/**)
            String webPath = "/uploads/" + newFilename;
            System.out.println("File saved successfully!");
            System.out.println("Web path: " + webPath);
            System.out.println("========================");

            return webPath;

        } catch (Exception e) {
            System.err.println("Error saving file: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error saving file: " + e.getMessage());
        }
    }
    /**
     * Xóa ảnh cũ của actor
     */
    private void deleteOldActorImage(Integer actorId) {
        try {
            String uploadPath = getUploadPath();
            String[] extensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

            System.out.println("=== DELETE OLD ACTOR IMAGE ===");
            System.out.println("Actor ID: " + actorId);
            System.out.println("Upload path: " + uploadPath);

            for (String ext : extensions) {
                File oldFile = new File(uploadPath, "actor_" + actorId + ext);
                System.out.println("Checking: " + oldFile.getAbsolutePath());
                if (oldFile.exists()) {
                    boolean deleted = oldFile.delete();
                    System.out.println("Deleted old image: " + oldFile.getName() + " - Success: " + deleted);
                }
            }
            System.out.println("===============================");
        } catch (Exception e) {
            System.err.println("Error deleting old images: " + e.getMessage());
        }
    }
    // ==================== CRUD OPERATIONS ====================

    // Trang danh sách diễn viên
    @GetMapping("")
    public String getAllActors(Model model) {
        List<ActorDTO> actors = actorService.getAllActors();

        // Sắp xếp theo ID tăng dần
        if (actors != null) {
            actors.sort(Comparator.comparing(ActorDTO::getActorID, Comparator.nullsLast(Comparator.naturalOrder())));
        }

        model.addAttribute("actors", actors);
        model.addAttribute("pageTitle", "Danh sách diễn viên");

        return "admin/list-actor";
    }

    // Chi tiết diễn viên
    @GetMapping("/{id}")
    public String getActorDetail(@PathVariable Integer id, Model model) {
        ActorDTO actor = actorService.getActorById(id);
        if (actor == null) {
            return "redirect:/admin/actors";
        }

        List<MovieDTO> movies = movieService.getMoviesByActor(id);

        model.addAttribute("actor", actor);
        model.addAttribute("movies", movies);
        model.addAttribute("pageTitle", "Chi tiết diễn viên - " + actor.getActorName());

        return "admin/detail-actor";
    }

    // Tìm kiếm diễn viên
    @GetMapping("/search")
    public String searchActors(@RequestParam(required = false) String keyword, Model model) {
        List<ActorDTO> actors;
        String pageTitle = "Kết quả tìm kiếm";

        if (keyword != null && !keyword.trim().isEmpty()) {
            actors = actorService.searchActorsByName(keyword);
            pageTitle = "Tìm kiếm diễn viên: " + keyword;
        } else {
            actors = actorService.getAllActors();
        }

        // Sắp xếp theo ID tăng dần
        if (actors != null) {
            actors.sort(Comparator.comparing(ActorDTO::getActorID, Comparator.nullsLast(Comparator.naturalOrder())));
        }

        model.addAttribute("actors", actors);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("keyword", keyword);

        return "admin/list-actor";
    }

    // Hiển thị form thêm actor mới
    @GetMapping("/add")
    public String addActorForm(Model model) {
        ActorDTO actor = new ActorDTO(); // Tạo object rỗng cho form

        // Lấy tất cả phim từ database
        List<MovieDTO> allMovies = movieService.getAllMovies();

        // DEBUG: Kiểm tra dữ liệu phim
        System.out.println("DEBUG - Loading " + (allMovies != null ? allMovies.size() : 0) + " movies for actor form");
        if (allMovies != null && !allMovies.isEmpty()) {
            System.out.println("DEBUG - First movie: " + allMovies.get(0).getMovieName());
        }

        model.addAttribute("actor", actor);
        model.addAttribute("allMovies", allMovies);
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Thêm diễn viên mới");

        return "admin/form-actor";
    }

    // Xử lý thêm actor mới với ảnh bắt buộc
    // Xử lý thêm actor mới với ảnh bắt buộc
    @PostMapping("/add")
    public String addActor(@ModelAttribute ActorDTO actorDTO,
                           @RequestParam(value = "selectedMovies", required = false) String selectedMovies,
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== ADD ACTOR START ===");
            System.out.println("Actor name: " + actorDTO.getActorName());
            System.out.println("Selected movies: " + selectedMovies);
            System.out.println("Image file: " + (imageFile != null ? imageFile.getOriginalFilename() : "null"));

            // Validate tên diễn viên
            if (actorDTO.getActorName() == null || actorDTO.getActorName().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên diễn viên không được để trống");
            }

            // ĐIỀU KIỆN MỚI: Bắt buộc phải có ảnh khi thêm diễn viên
            if (imageFile == null || imageFile.isEmpty()) {
                throw new IllegalArgumentException("Ảnh diễn viên là bắt buộc khi thêm mới");
            }

            // Validate ảnh upload ngay từ đầu
            String originalFilename = imageFile.getOriginalFilename();
            if (originalFilename == null) {
                throw new IllegalArgumentException("Tên file ảnh không hợp lệ");
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!fileExtension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                throw new IllegalArgumentException("Chỉ chấp nhận file ảnh (jpg, jpeg, png, gif, webp)");
            }

            if (imageFile.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
            }

            // Đặt ảnh mặc định tạm thời
            actorDTO.setImage("/uploads/default-actor.jpg");

            // Lưu actor trước để có ID
            ActorDTO savedActor = actorService.saveActor(actorDTO);
            Integer savedActorId = savedActor.getActorID();
            System.out.println("Actor saved with ID: " + savedActorId);

            // XỬ LÝ UPLOAD ẢNH - BẮT BUỘC PHẢI THÀNH CÔNG
            try {
                String uploadedImagePath = uploadActorImage(imageFile, savedActorId);
                if (uploadedImagePath == null) {
                    // Nếu upload thất bại, xóa actor đã tạo và báo lỗi
                    actorService.deleteActor(savedActorId);
                    throw new IOException("Không thể upload ảnh diễn viên");
                }

                // Cập nhật lại đường dẫn ảnh trong database
                savedActor.setImage(uploadedImagePath);
                actorService.updateActor(savedActor);
                System.out.println("Updated actor image path: " + uploadedImagePath);

            } catch (IOException e) {
                // Nếu có lỗi upload, xóa actor đã tạo
                try {
                    actorService.deleteActor(savedActorId);
                    System.err.println("Deleted actor due to image upload failure: " + savedActorId);
                } catch (Exception deleteEx) {
                    System.err.println("Error deleting actor after upload failure: " + deleteEx.getMessage());
                }
                throw new IllegalArgumentException("Lỗi khi upload ảnh diễn viên: " + e.getMessage());
            }

            // SỬA: Xử lý danh sách phim đã chọn với validation
            if (selectedMovies != null && !selectedMovies.trim().isEmpty()) {
                try {
                    List<Integer> movieIds = Arrays.stream(selectedMovies.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .filter(s -> !s.equals("NaN")) // Lọc bỏ NaN
                            .map(s -> {
                                try {
                                    return Integer.parseInt(s);
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid movie ID: " + s);
                                    return null;
                                }
                            })
                            .filter(id -> id != null && id > 0) // Chỉ lấy ID hợp lệ
                            .collect(Collectors.toList());

                    if (!movieIds.isEmpty()) {
                        // Cập nhật quan hệ Actor-Movie
                        actorService.updateActorMovies(savedActorId, movieIds);
                        System.out.println("Actor assigned to " + movieIds.size() + " movies: " + movieIds);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing movie IDs: " + e.getMessage());
                    // Không throw exception vì actor đã được tạo thành công
                }
            }

            System.out.println("=== ADD ACTOR SUCCESS ===");
            redirectAttributes.addFlashAttribute("success", "Thêm diễn viên thành công!");
            return "redirect:/admin/actors/" + savedActorId;

        } catch (Exception e) {
            System.err.println("=== ADD ACTOR ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();

            // Lấy lại danh sách phim khi có lỗi
            List<MovieDTO> allMovies = movieService.getAllMovies();

            model.addAttribute("actor", actorDTO);
            model.addAttribute("allMovies", allMovies);
            model.addAttribute("isEdit", false);
            model.addAttribute("pageTitle", "Thêm diễn viên mới");
            model.addAttribute("error", "Có lỗi xảy ra khi thêm diễn viên: " + e.getMessage());

            return "admin/form-actor";
        }
    }


    // Hiển thị form chỉnh sửa actor
    @GetMapping("/edit/{id}")
    public String editActorForm(@PathVariable Integer id, Model model) {
        ActorDTO actor = actorService.getActorById(id);
        if (actor == null) {
            return "redirect:/admin/actors";
        }

        // Lấy tất cả phim và phim hiện tại của actor
        List<MovieDTO> allMovies = movieService.getAllMovies();
        List<MovieDTO> actorMovies = movieService.getMoviesByActor(id);

        // DEBUG: Kiểm tra dữ liệu
        System.out.println("DEBUG - Edit form: " + (allMovies != null ? allMovies.size() : 0) + " total movies");
        System.out.println("DEBUG - Actor movies: " + (actorMovies != null ? actorMovies.size() : 0) + " movies");
        System.out.println("DEBUG - Actor current image: " + actor.getImage());

        // Tạo danh sách ID phim hiện tại
        List<Integer> currentMovieIds = actorMovies.stream()
                .map(MovieDTO::getMovieID)
                .collect(Collectors.toList());

        model.addAttribute("actor", actor);
        model.addAttribute("allMovies", allMovies);
        model.addAttribute("currentMovieIds", currentMovieIds);
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Chỉnh sửa diễn viên - " + actor.getActorName());

        return "admin/edit-actor";
    }

    // Xử lý cập nhật actor
    @PostMapping("/edit/{id}")
    public String updateActor(@PathVariable Integer id,
                              @ModelAttribute ActorDTO actorDTO,
                              @RequestParam(value = "movieIds", required = false) List<Integer> movieIds,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== DEBUG UPDATE ACTOR ===");
            System.out.println("Actor ID: " + id);
            System.out.println("Movie IDs: " + movieIds);

            // Đảm bảo ID được set đúng
            actorDTO.setActorID(id);

            // Validate tên diễn viên
            if (actorDTO.getActorName() == null || actorDTO.getActorName().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên diễn viên không được để trống");
            }

            // Lấy thông tin actor hiện tại để giữ ảnh cũ
            ActorDTO existingActor = actorService.getActorById(id);
            String currentImagePath = existingActor.getImage();

            // XỬ LÝ UPLOAD ẢNH MỚI (không bắt buộc khi edit)
            try {
                if (imageFile != null && !imageFile.isEmpty()) {
                    String uploadedImagePath = uploadActorImage(imageFile, id);
                    if (uploadedImagePath != null) {
                        actorDTO.setImage(uploadedImagePath);
                        System.out.println("Đã upload ảnh actor mới: " + uploadedImagePath);
                    } else {
                        // Giữ ảnh cũ nếu upload thất bại
                        actorDTO.setImage(currentImagePath);
                    }
                } else {
                    // Không upload ảnh mới, giữ ảnh cũ
                    actorDTO.setImage(currentImagePath);
                }
            } catch (IOException e) {
                System.err.println("Lỗi upload ảnh actor: " + e.getMessage());
                // Giữ ảnh cũ và thông báo warning
                actorDTO.setImage(currentImagePath);
                redirectAttributes.addFlashAttribute("warning", "Lỗi upload ảnh: " + e.getMessage());
            }

            // Cập nhật thông tin actor
            ActorDTO updatedActor = actorService.updateActor(actorDTO);

            // Xử lý cập nhật danh sách phim
            if (movieIds != null && !movieIds.isEmpty()) {
                System.out.println("DEBUG - Updating actor with " + movieIds.size() + " movies");
            } else {
                System.out.println("DEBUG - Removing actor from all movies");
                movieIds = List.of(); // Empty list để xóa tất cả
            }

            // Cập nhật quan hệ Actor-Movie
            actorService.updateActorMovies(id, movieIds);

            redirectAttributes.addFlashAttribute("success", "Cập nhật diễn viên thành công!");
            return "redirect:/admin/actors/" + id;

        } catch (Exception e) {
            System.err.println("ERROR - Updating actor: " + e.getMessage());
            e.printStackTrace();

            // Lấy lại dữ liệu khi có lỗi
            List<MovieDTO> allMovies = movieService.getAllMovies();
            List<MovieDTO> actorMovies = movieService.getMoviesByActor(id);
            List<Integer> currentMovieIds = actorMovies != null ?
                    actorMovies.stream().map(MovieDTO::getMovieID).collect(Collectors.toList()) :
                    new ArrayList<>();

            model.addAttribute("actor", actorDTO);
            model.addAttribute("allMovies", allMovies);
            model.addAttribute("currentMovieIds", currentMovieIds);
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Chỉnh sửa diễn viên - " + actorDTO.getActorName());
            model.addAttribute("error", "Có lỗi xảy ra khi cập nhật diễn viên: " + e.getMessage());

            return "admin/edit-actor";
        }
    }

    // Xóa actor
    @PostMapping("/delete/{id}")
    public String deleteActor(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            ActorDTO actor = actorService.getActorById(id);
            if (actor == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy diễn viên!");
                return "redirect:/admin/actors";
            }

            // Kiểm tra xem diễn viên có đang tham gia phim nào không
            List<MovieDTO> movies = movieService.getMoviesByActor(id);
            if (movies != null && !movies.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "Không thể xóa diễn viên đang tham gia " + movies.size() + " phim!");
                return "redirect:/admin/actors/" + id;
            }

            // Xóa ảnh trước khi xóa actor
            deleteOldActorImage(id);

            // Xóa actor
            actorService.deleteActor(id);

            System.out.println("DEBUG - Actor deleted successfully: " + actor.getActorName());
            redirectAttributes.addFlashAttribute("success", "Xóa diễn viên thành công!");
            return "redirect:/admin/actors";
        } catch (Exception e) {
            System.err.println("ERROR - Deleting actor: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa diễn viên: " + e.getMessage());
            return "redirect:/admin/actors/" + id;
        }
    }

    // ==================== DEBUG ENDPOINTS ====================

    /**
     * Debug endpoint để kiểm tra cấu hình
     */
    @GetMapping("/debug/config")
    @ResponseBody
    public String debugConfig() {
        StringBuilder result = new StringBuilder();
        String uploadPath = getUploadPath();

        result.append("=== ACTOR CONTROLLER CONFIG DEBUG ===\n");
        result.append("Upload dir từ config: ").append(uploadDir).append("\n");
        result.append("Upload path tuyệt đối: ").append(uploadPath).append("\n");
        result.append("Directory exists: ").append(new File(uploadPath).exists()).append("\n");
        result.append("Directory can write: ").append(new File(uploadPath).canWrite()).append("\n");
        result.append("Directory can read: ").append(new File(uploadPath).canRead()).append("\n");

        // WebConfig path comparison
        String webConfigPath = Paths.get(uploadDir).toFile().getAbsolutePath() + "/";
        result.append("WebConfig path: ").append(webConfigPath).append("\n");
        result.append("Paths match: ").append(uploadPath.equals(webConfigPath)).append("\n");

        // List files in uploads
        File dir = new File(uploadPath);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            result.append("Total files: ").append(files != null ? files.length : 0).append("\n");
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith("actor_")) {
                        result.append("- ").append(file.getName())
                                .append(" (").append(file.length()).append(" bytes)")
                                .append("\n");
                    }
                }
            }
        }

        return result.toString();
    }

    /**
     * Debug endpoint để kiểm tra uploads
     */
    @GetMapping("/debug/uploads")
    @ResponseBody
    public String debugActorUploads() {
        StringBuilder result = new StringBuilder();
        String uploadPath = getUploadPath();

        // Kiểm tra thư mục uploads
        File uploadDir = new File(uploadPath);
        result.append("Upload Directory: ").append(uploadDir.getAbsolutePath()).append("\n");
        result.append("Exists: ").append(uploadDir.exists()).append("\n");
        result.append("Can Write: ").append(uploadDir.canWrite()).append("\n");
        result.append("Can Read: ").append(uploadDir.canRead()).append("\n");

        if (uploadDir.exists()) {
            File[] files = uploadDir.listFiles();
            result.append("Files count: ").append(files != null ? files.length : 0).append("\n");
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith("actor_")) {
                        result.append("- ").append(file.getName())
                                .append(" (").append(file.length()).append(" bytes)")
                                .append("\n");
                    }
                }
            }
        }

        return result.toString();
    }

    /**
     * Test endpoint để test URL access
     */
    @GetMapping("/test-image/{filename}")
    @ResponseBody
    public String testImageAccess(@PathVariable String filename) {
        String uploadPath = getUploadPath();
        File imageFile = new File(uploadPath, filename);

        StringBuilder result = new StringBuilder();
        result.append("Testing image: ").append(filename).append("\n");
        result.append("Full path: ").append(imageFile.getAbsolutePath()).append("\n");
        result.append("File exists: ").append(imageFile.exists()).append("\n");
        result.append("File readable: ").append(imageFile.canRead()).append("\n");
        result.append("File size: ").append(imageFile.length()).append(" bytes\n");

        // Test URL that should work
        result.append("URL to test: http://localhost:8080/uploads/").append(filename).append("\n");

        return result.toString();
    }
}