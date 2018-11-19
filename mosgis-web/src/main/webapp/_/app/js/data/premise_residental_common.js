define ([], function () {

    var form_name = 'premise_residental_common_form'
    
    $_DO.restore_premise_residental_common = function (e) {
    
        if (!confirm ('Удалить запись об аннулировании объекта?')) return 
        
        query ({
        
            type: 'premises_residental', 
            id: $_REQUEST.id, 
            action: 'update'
            
        }, {data: {
        
            terminationdate: null,
            annulmentinfo: null,
            code_vc_nsi_330: null,
        
        }}, reload_page)        

    }    

    $_DO.annul_premise_residental_common = function (e) {
    
        use.block ('annul_popup')

    }   
    
    $_DO.choose_tab_premise_residental_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['premise_residental_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('premise_residental_common.active_tab', name)

        use.block (name)
    
    }

    $_DO.cancel_premise_residental_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'premises_residental'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_premise_residental_common = function (e) {

        var data = {item: w2ui [form_name].record}

        if (data.item.is_annuled) {
            alert ('Поскольку данный объект жилого фонда аннулирован, редактирование невозможно.')
            return e.preventDefault ()
        }       

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_premise_residental_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
                                
        if (v.premisesnum == null || v.premisesnum == '') die ('premisesnum', 'Укажите, пожалуйста, номер помещения')
        
        if (!/[0-9А-ЯЁа-яёA-Za-z]/.test (v.premisesnum)) die ('premisesnum', 'Некорректный номер помещения')
        
        if (!v.code_vc_nsi_30) die ('code_vc_nsi_30', 'Необходимо указать характеристику помещения')
        if (parseFloat (v.totalarea || '0') < 0.01) die ('totalarea', 'Необходимо указать размер общей плошади')
        if (parseFloat (v.grossarea || '0') < 0.01) die ('grossarea', 'Необходимо указать размер жилой плошади')

        query ({type: 'premises_residental', action: 'update'}, {data: v}, reload_page)

    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = $('body').data ('data')

        data.__read_only = 1
        
        data.active_tab = localStorage.getItem ('premise_residental_common.active_tab') || 'premise_residental_common_living_rooms'        

        done (data)
        
    }

})