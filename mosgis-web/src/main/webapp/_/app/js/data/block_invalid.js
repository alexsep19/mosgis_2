define ([], function () {

    var form_name = 'block_invalid_form'

    $_DO.download_block_invalid = function (e) {    

        var box = w2ui [form_name].box

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({

            type:   'house_docs', 
            id:     $('body').data ('data').file.uuid,
            action: 'download',

        }, {}, {

            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},

            onload: function () {w2utils.unlock (box)},

        })
    
    }
    
    $_DO.cancel_block_invalid = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'blocks'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_block_invalid = function (e) {

        var data = {item: w2ui [form_name].record}
        
        if (data.item.is_annuled) {
            alert ('Поскольку данный объект жилого фонда аннулирован, редактирование невозможно.')
            return e.preventDefault ()
        }       

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_block_invalid = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
        
        if (v.file) {
        
            var r = f.record;            
            
            if (!r.f_20127) die ('f_20127', 'Укажите, пожалуйста, основание признания данного помещения непригодным для проживания')
            
            if (!r.f_20128) {
                $('input[name=f_20128]').focus ()
                return alert ('Укажите, пожалуйста, дату документа');
            }

            if (!/^\d\d\.\d\d\.\d\d\d\d$/.test (r.f_20128)) {
                $('input[name=f_20128]').focus ()
                return alert ('Некорректная дата документа');
            }

            if (!r.f_20129) {
                $('input[name=f_20129]').focus ()
                return alert ('Укажите, пожалуйста, номер документа');
            }

            if (!r.file || r.file.length == 0) {
                $('input[name=file]').focus ()
                return alert ('Вы не указали файл');
            };

            var n = r.file.length

            var exts   = {pdf:1, doc:1, docx:1, rtf:1, xls:1, xlsx:1, jpg:1, jpeg:1}
            var max_mb = 10
            var sum_size = 0;

            for (var i = 0; i < n; i ++) {

                var file = r.file [i].file

                var fn = file.name

                if (fn.length > 255) return alert ('Некорректное имя файла: ' + fn + '. Согласно требованиям ГИС ЖКХ, его длина не может превышать 255 символов')

                var parts = fn.split ('.')         

                if (parts.length < 2) return alert ('Некорректное имя файла: ' + fn + ' (невозможно определить расширение)')

                var ext = parts [parts.length - 1];

                if (!exts [ext]) {

                    var l = []; for (var e in exts) l.push (e)

                    return alert ('Некорректное имя файла: ' + fn + '.\n\nСогласно требованиям ГИС ЖКХ, разрешены следующие: ' + l.sort ().join (', ') + '.')

                }

                if (file.size > max_mb * 1024 * 1024) return alert ('Файл ' + fn + ' имеет недопустимо большой объём. Согласно требованиям ГИС ЖКХ, его величина не может превышать ' + max_mb + ' Мб.')

                sum_size += file.size

            }            
            
            $('.w2ui-buttons button').hide ()
        
            var $progress = $('progress')

            $progress.prop ({max: sum_size, value: 0}).show ()    

            var portion = 128 * 1024;
            var sum = 0;

            f.lock ();

            var contract = $('body').data ('data')

            var data = {
                uuid         : $_REQUEST.id,
                no           : v.f_20129,
                dt           : v.f_20128,
                id_file_type : "20820",
                note         : "",
                ref          : "uuid_block",
            }

            var n = r.file.length

            var check = setInterval (function () {
                if (n) return
                clearInterval (check)
                $_DO.apologize = $.noop
                query ({type: 'blocks', action: 'update'}, {data: {f_20127: v.f_20127}}, reload_page)
//                reload_page ()
            }, 100)

            var opt = {
                data       : data,
                type       : 'house_docs',
                onprogress : function (x, y) {sum += portion; $progress.val (sum)},
                onloadend  : function () {n --}
            }
            
            function upload_file () {
                $.each (r.file, function () {Base64file.upload (this.file, opt)})            
            }

            var file = $('body').data ('data').file
            
            if (file) {
            
                query ({
                    type:   'house_docs', 
                    id:     file.uuid,
                    action: 'delete',
                }, {}, upload_file)
            
            }
            else {
                upload_file ()
            }                
        
        }
        else {
            query ({type: 'blocks', action: 'update'}, {data: v}, reload_page)
        }        

    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = $('body').data ('data')

        data.__read_only = 1
        
        done (data)
        
    }

})