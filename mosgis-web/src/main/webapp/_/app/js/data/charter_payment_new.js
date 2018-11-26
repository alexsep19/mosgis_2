define ([], function () {

    function upload_gis_file ($progress, file, type, data, done) {
    
        $progress.prop ({max: file.size, value: 0}).show ().css ({visibility: 'visible'})
    
        Base64file.upload (file, {type: type, {data: data}, 
            onprogress: function (x, y) {$progress.val (x)},
            onloadend: done
        })
    
    }

    $_DO.update_charter_payment_new = function (e) {

        var form = w2ui ['charter_payment_new_form']

        var v = form.values ()
        var r = form.record

        var it = $('body').data ('data').item
        
        if (!v.begindate) die ('begindate', 'Укажите, пожалуйста, дату начала')

        if (!v.enddate) die ('enddate', 'Укажите, пожалуйста, дату окончания')
        if (v.enddate < v.begindate) die ('enddate', 'Дата начала превышает дату окончания управления')
        
        if (v.payment_1) {
            if (!r.file_1) die ('file_1', 'Загрузите, пожалуйста, протокол')
            validate_gis_file ('file_1', r.file_1 [0].file)
        }
        else {
            if (r.file_1) die ('payment_1', 'Вы загрузили протокол, но не указали размер платы')
        }

        if (v.payment_0) {
            if (!r.file_0) die ('file_0', 'Загрузите, пожалуйста, протокол')
            validate_gis_file ('file_0', r.file_0 [0].file)
        }
        else {
            if (r.file_0) die ('payment_0', 'Вы загрузили протокол, но не указали размер платы')
        }

        v.uuid_charter = $_REQUEST.id
        
        function finish (id) {
        
            w2popup.close ()

            w2confirm ('Услуга зарегистрирована. Открыть её страницу в новой вкладке?').yes (function () {openTab ('/charter_payment/' + id)})
            
            var grid = w2ui ['charter_payments_grid']

            grid.reload (grid.refresh)
            
        }
darn (r)        
        form.lock ()

        $('.w2ui-popup-body progress').css ({visibility: 'visible'})
return        
        query ({type: 'charter_payments', action: 'create', id: undefined}, {data: v}, function (data) {
        
            function exit () {return finish (data.id)}
        
            function check_file_0 () {

                if (!r.file_0) return exit ()
                
                upload_gis_file ($('#progress_0'), r.file_0 [0].file, 'charter_payment_docs', 
                
                    data: {
                        uuid_charter_payment: data.id, 
                        description: r.file_0 [0].file.name
                    },
                    
                    function (uuid_file) {

                        query ({type: 'charter_payments', action: 'create', id: data.id}, {data: {uuid_file_0: uuid_file}}, function (d) {
                        
                            exit ()
                        
                        })

                    }
                    
                )                
            
            }
        
            check_file_0 ()
                    
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = {
            uuid_charter_object: "",
            begindate: dt_dmy (data.item.date_),
        }
        
        var root = data.charter_objects
                
        var a = []

        if (root.length == 1) {
            data.record.uuid_charter_object = root [0].id
        }
        else {
            a.push ({id: "", text: "Все объекты устава"})
        }
        
        $.each (root, function () {
            a.push (this)
        })
        
        data.objects = a
        
        done (data)

    }

})