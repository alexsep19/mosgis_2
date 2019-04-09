define ([], function () {

    var form_name = 'payment_document_common_form'
/*    
    $_DO.approve_payment_document_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'payment_documents', action: 'approve'}, {}, reload_page)
    }
        
    $_DO.alter_payment_document_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'payment_documents', action: 'alter'}, {}, reload_page)
    }          
*/    
    $_DO.cancel_payment_document_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'payment_documents'}, {}, function (data) {

            data.__read_only = true
            
            var it = data.item
            
            fix (it)

            w2ui ['passport_layout'].get ('main').tabs.enable ('payment_document_common_charge_info', 'payment_document_common_log')

            $_F5 (data)

        })

    }

    $_DO.edit_payment_document_common = function (e) {

        $_SESSION.set ('edit_payment_document_common', 1)

        var data = {item: w2ui [form_name].record}

        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false

        var $form = w2ui [form_name]
        
        var tabs = w2ui ['passport_layout'].get ('main').tabs        
        tabs.disable ('payment_document_common_charge_info', 'payment_document_common_log')
        tabs.click ('payment_document_common_additional_information')

    }    

    $_DO.update_payment_document_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
        
        v.debtpreviousperiods *= (- v.sign)
        
        v.additionalinformation = $('textarea').val () || ''
        
        if (v.additionalinformation.length > 1000) die ('additionalinformation', 'Дополнительная информация не может содержать более 1000 символов')
        
        query ({type: 'payment_documents', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_payment_document_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'payment_documents', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_payment_document_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('payment_document_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    function fix (it) {
    
        it.status_label = $('body').data ('data').vc_gis_status [it.id_ctr_status]
        
        it.sign = it.debtpreviousperiods > 0 ? -1 : 1

        it.debtpreviousperiods *= (- it.sign)

    }    

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('payment_document_common.active_tab') || 'payment_document_common_charge_info'

        data.__read_only = 1
        
        var it = data.item
        
        fix (it)
                
        done (data)
        
    }

})