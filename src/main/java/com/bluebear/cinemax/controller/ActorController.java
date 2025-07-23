package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.ActorDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.service.ActorService;
import com.bluebear.cinemax.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Thư mục upload
    private static final String UPLOAD_DIR = "uploads/";

    // ==================== PHƯƠNG THỨC UPLOAD ẢNH ACTOR ====================

    /**
     * Upload file ảnh actor và trả về đường dẫn
     */
    private String uploadActorImage(MultipartFile file, Integer actorId) throws IOException {
        System.out.println("=== DEBUG UPLOAD ACTOR IMAGE ===");
        System.out.println("File: " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("Actor ID: " + actorId);
        System.out.println("File empty: " + (file != null ? file.isEmpty() : "file is null"));

        if (file == null || file.isEmpty()) {
            System.out.println("File null hoặc empty, return null");
            return null;
        }

        // Kiểm tra định dạng file
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("Tên file không hợp lệ");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        System.out.println("File extension: " + fileExtension);

        if (!fileExtension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IOException("Chỉ chấp nhận file ảnh (jpg, jpeg, png, gif, webp)");
        }

        // Kiểm tra kích thước file (max 5MB)
        System.out.println("File size: " + file.getSize() + " bytes");
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IOException("Kích thước file không được vượt quá 5MB");
        }

        // Tạo thư mục nếu chưa tồn tại
        File uploadDir = new File(UPLOAD_DIR);
        System.out.println("Upload directory path: " + uploadDir.getAbsolutePath());
        System.out.println("Upload directory exists: " + uploadDir.exists());

        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            System.out.println("Created upload directory: " + created);
            if (!created) {
                throw new IOException("Không thể tạo thư mục upload: " + uploadDir.getAbsolutePath());
            }
        }

        // Kiểm tra quyền ghi
        if (!uploadDir.canWrite()) {
            throw new IOException("Không có quyền ghi vào thư mục: " + uploadDir.getAbsolutePath());
        }

        // Xóa ảnh cũ nếu có
        if (actorId != null) {
            deleteOldActorImage(actorId);
        }

        // Tạo tên file theo format: actor_{actorId}.{extension}
        String uniqueFilename = "actor_" + actorId + fileExtension;
        Path filePath = Paths.get(UPLOAD_DIR + uniqueFilename);
        System.out.println("Full file path: " + filePath.toAbsolutePath());

        try {
            // Lưu file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File saved successfully");

            // Kiểm tra file đã được tạo thành công
            File savedFile = filePath.toFile();
            System.out.println("File exists after save: " + savedFile.exists());
            System.out.println("File size after save: " + savedFile.length() + " bytes");

            if (!savedFile.exists()) {
                throw new IOException("File không được lưu thành công");
            }

            // Trả về đường dẫn relative
            String relativePath = "/uploads/" + uniqueFilename;
            System.out.println("Returning relative path: " + relativePath);
            System.out.println("=========================");
            return relativePath;

        } catch (Exception e) {
            System.err.println("Error saving file: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Lỗi khi lưu file: " + e.getMessage());
        }
    }

    /**
     * Xóa ảnh cũ của actor
     */
    private void deleteOldActorImage(Integer actorId) {
        System.out.println("=== DEBUG DELETE OLD ACTOR IMAGE ===");
        System.out.println("Actor ID: " + actorId);

        String[] extensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        for (String ext : extensions) {
            String oldFilename = "actor_" + actorId + ext;
            File oldFile = new File(UPLOAD_DIR + oldFilename);
            System.out.println("Checking file: " + oldFile.getAbsolutePath());
            System.out.println("File exists: " + oldFile.exists());

            if (oldFile.exists()) {
                boolean deleted = oldFile.delete();
                System.out.println("Deleted: " + deleted + " - " + oldFilename);
            }
        }
        System.out.println("==============================");
    }

    // ==================== CRUD OPERATIONS ====================

    // Trang danh sách diễn viên
    @GetMapping("")
    public String getAllActors(Model model) {
        List<ActorDTO> actors = actorService.getAllActors();

        // Sắp xếp theo ID tăng dần
        if (actors != null) {
            actors.sort(Comparator.comparing(ActorDTO::getActorId, Comparator.nullsLast(Comparator.naturalOrder())));
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
            actors.sort(Comparator.comparing(ActorDTO::getActorId, Comparator.nullsLast(Comparator.naturalOrder())));
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

    // Xử lý thêm actor mới
    @PostMapping("/add")
    public String addActor(@ModelAttribute ActorDTO actorDTO,
                           @RequestParam(value = "selectedMovies", required = false) String selectedMovies,
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            // Validate tên diễn viên
            if (actorDTO.getActorName() == null || actorDTO.getActorName().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên diễn viên không được để trống");
            }

            // Đặt ảnh mặc định tạm thời
            actorDTO.setImage("/uploads/default-actor.jpg");

            // Lưu actor trước để có ID
            ActorDTO savedActor = actorService.saveActor(actorDTO);
            Integer savedActorId = savedActor.getActorId();

            // XỬ LÝ UPLOAD ẢNH SAU KHI CÓ ID
            String finalImagePath = "/uploads/default-actor.jpg";

            try {
                // Upload ảnh nếu có
                if (imageFile != null && !imageFile.isEmpty()) {
                    String uploadedImagePath = uploadActorImage(imageFile, savedActorId);
                    if (uploadedImagePath != null) {
                        finalImagePath = uploadedImagePath;
                        System.out.println("Đã upload ảnh actor: " + finalImagePath);

                        // Cập nhật lại đường dẫn ảnh trong database
                        savedActor.setImage(finalImagePath);
                        actorService.updateActor(savedActor);
                    }
                }
            } catch (IOException e) {
                System.err.println("Lỗi upload ảnh actor: " + e.getMessage());
                // Không return error vì actor đã được tạo thành công
                redirectAttributes.addFlashAttribute("warning",
                        "Diễn viên đã được thêm thành công nhưng có lỗi khi upload ảnh: " + e.getMessage());
            }

            // Xử lý danh sách phim đã chọn
            if (selectedMovies != null && !selectedMovies.trim().isEmpty()) {
                List<Integer> movieIds = Arrays.stream(selectedMovies.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());

                // Cập nhật quan hệ Actor-Movie
                actorService.updateActorMovies(savedActorId, movieIds);

                System.out.println("DEBUG - Actor assigned to " + movieIds.size() + " movies");
            }

            redirectAttributes.addFlashAttribute("success", "Thêm diễn viên thành công!");
            return "redirect:/admin/actors/" + savedActorId;

        } catch (Exception e) {
            // Lấy lại danh sách phim khi có lỗi
            List<MovieDTO> allMovies = movieService.getAllMovies();

            model.addAttribute("actor", actorDTO);
            model.addAttribute("allMovies", allMovies);
            model.addAttribute("isEdit", false);
            model.addAttribute("pageTitle", "Thêm diễn viên mới");
            model.addAttribute("error", "Có lỗi xảy ra khi thêm diễn viên: " + e.getMessage());

            System.err.println("ERROR - Adding actor: " + e.getMessage());
            e.printStackTrace();

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
                .map(MovieDTO::getMovieId)
                .collect(Collectors.toList());

        model.addAttribute("actor", actor);
        model.addAttribute("allMovies", allMovies);
        model.addAttribute("currentMovieIds", currentMovieIds);
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Chỉnh sửa diễn viên - " + actor.getActorName());

        // Thay đổi đường dẫn return để khớp với file HTML hiện tại
        return "/admin/edit-actor";
    }

    // Xử lý cập nhật actor
    // Cập nhật method trong ActorController.java

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
            actorDTO.setActorId(id);

            // Validate tên diễn viên
            if (actorDTO.getActorName() == null || actorDTO.getActorName().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên diễn viên không được để trống");
            }

            // Lấy thông tin actor hiện tại để giữ ảnh cũ
            ActorDTO existingActor = actorService.getActorById(id);
            String currentImagePath = existingActor.getImage();

            // XỬ LÝ UPLOAD ẢNH MỚI
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
                    actorMovies.stream().map(MovieDTO::getMovieId).collect(Collectors.toList()) :
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
     * Debug endpoint để kiểm tra uploads
     */
    @GetMapping("/debug/uploads")
    @ResponseBody
    public String debugActorUploads() {
        StringBuilder result = new StringBuilder();

        // Kiểm tra thư mục uploads
        File uploadDir = new File(UPLOAD_DIR);
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
}