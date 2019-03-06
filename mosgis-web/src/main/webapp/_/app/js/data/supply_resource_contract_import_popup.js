define ([], function () {

    $_DO.update_supply_resource_contract_import_popup = function (e) {

        var f = w2ui ['supply_resource_contract_import_popup_form']

        var v = f.values ()        
        var fl = v.files    
        if (!fl) die ('files', 'Укажите, пожалуйста, файл')    
        var file = fl [0].file
        if (file.size == 0) die ('files', 'Пожалуйста, укажите не пустой файл')
        if (!/\.xlsx$/.test (file.name)) die ('files', 'Некорректный формат файла')                
                                                  
        Base64file.upload (file, {
        
            type: 'in_xl_files',

            data: {
                id_type: 3
            },
            
            onprogress: show_popup_progress (file.size),
            
            onloadend: function (id) {
                alert ('Файл принят в обработку')
                w2popup.close ()
                use.block('supply_resource_contracts')
            }

        })

    }

    return function (done) {
    
        done (clone ($('body').data ('data')))
        
    }
    
})