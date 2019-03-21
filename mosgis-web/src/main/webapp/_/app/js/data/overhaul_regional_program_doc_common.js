define ([], function () {

    var form_name = 'overhaul_regional_program_doc_common_form'

    $_DO.cancel_overhaul_regional_program_doc_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'overhaul_regional_program_documents'}, {}, function (data) {

            data.__read_only = true
            
            var it = data.item

            $_F5 (data)

        })

    }
    
    $_DO.edit_overhaul_regional_program_doc_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_overhaul_regional_program_doc_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var it = $('body').data ('data').item
        
        var f = w2ui [form_name]
        var v = f.values ()
        
        if (!v.code_nsi_79) die ('code_nsi_79', 'Пожалуйста, укажите вид документа')
        if (!v.number_) die ('number_', 'Пожалуйста, укажите номер документа')
        if (!v.date_) die ('date_', 'Пожалуйстаб укажите, дату')    
        if (!v.fullname) die ('fullname', 'Пожалуйста, укажите наименование')
        if (!v.legislature) die ('legislature', 'Пожалуйста, укажите орган власти, принявший документ')
                                               
        query ({type: 'overhaul_regional_program_documents', action: 'update'}, {data: v}, reload_page)
        
    }
    
    $_DO.delete_overhaul_regional_program_doc_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'overhaul_regional_program_documents', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_overhaul_regional_program_doc_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('overhaul_regional_program_doc_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        var it = data.item

        data.active_tab = localStorage.getItem ('overhaul_regional_program_doc_common.active_tab') || 'overhaul_regional_program_common_log'

        data.__read_only = 1
                        
        done (data)
        
    }

})