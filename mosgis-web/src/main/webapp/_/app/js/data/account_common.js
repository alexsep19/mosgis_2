define ([], function () {

    var form_name = 'account_common_form'
    
    $_DO.cancel_account_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'accounts'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_account_common = function (e) {

        var data = {item: w2ui [form_name].record}

        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_account_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
/*                                
        if (!v.additionalservicetypename) die ('additionalservicetypename', 'Укажите, пожалуйста, наименование услуги')
        if (!v.okei)                      die ('okei', 'Укажите, пожалуйста, единицы измерения')
*/
        query ({type: 'accounts', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_account_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'accounts', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_account_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('account_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('account_common.active_tab') || 'account_common_log'

        data.__read_only = 1

/*        
        data.item.status_label = data.vc_async_entity_states [data.item.id_status]
        data.item.err_text = data.item ['out_soap.err_text']
*/        
        data.item._can = $_USER.role.admin /*|| data.item.id_status == 10*/ ? {} : {
            edit: 1 - data.item.is_deleted,
            update: 1,
            cancel: 1,
            delete: 1 - data.item.is_deleted,
        }

        done (data)
        
    }

})