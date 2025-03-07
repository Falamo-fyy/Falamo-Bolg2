$(function() {
    let editor = editormd("editor", {
        path: "/editormd/lib/",
        height: 640,
        saveHTMLToTextarea: true,  // 保存HTML到textarea
        tex: true,                 // 开启科学公式
        flowChart: true,           // 开启流程图
        sequenceDiagram: true,     // 开启时序图
        toolbarIcons: "full",      // 使用完整工具栏
        imageUpload: true,
        imageFormats: ["jpg", "jpeg", "gif", "png", "bmp", "webp"],
        imageUploadURL: "/api/upload"  // 文件上传接口
    });
}); 