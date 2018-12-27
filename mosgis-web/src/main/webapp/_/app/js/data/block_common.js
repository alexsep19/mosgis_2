define ([], function () {

    var form_name = 'block_common_form'
    
    $_DO.restore_block_common = function (e) {
    
        if (!confirm ('Удалить запись об аннулировании объекта?')) return 
        
        query ({
        
            type: 'blocks', 
            id: $_REQUEST.id, 
            action: 'restore'
            
        }, {}, reload_page)        

    }    

    $_DO.annul_block_common = function (e) {
    
        use.block ('annul_popup')

    }    
    
    $_DO.cancel_block_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'blocks'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_block_common = function (e) {

        var data = {item: w2ui [form_name].record}

        if (data.item.is_annuled) {
            alert ('Поскольку данный объект жилого фонда аннулирован, редактирование невозможно.')
            return e.preventDefault ()
        }       

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_block_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
                                
        if (v.blocknum == null || v.blocknum == '') die ('blocknum', 'Укажите, пожалуйста, номер блока')
        
        if (!/[0-9А-ЯЁа-яёA-Za-z]/.test (v.blocknum)) die ('blocknum', 'Некорректный номер блока')

        if (v.is_nrs == 0 && !v.code_vc_nsi_30) die ('code_vc_nsi_30', 'Необходимо указать характеристику блока')
        if (parseFloat (v.totalarea || '0') < 0.01) die ('totalarea', 'Необходимо указать размер общей плошади')
        if (v.is_nrs == 0 && parseFloat (v.grossarea || '0') < 0.01) die ('grossarea', 'Необходимо указать размер жилой плошади')

        query ({type: 'blocks', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.choose_tab_block_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('block_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('block_common.active_tab') || 'block_common_living_rooms'

        data.__read_only = 1

        data.item.cat = data.item.is_nrs ? 'Нежилое помещение' : 'Жилое помещение'

        done (data)
        
    }

})