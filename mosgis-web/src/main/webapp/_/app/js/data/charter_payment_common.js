define ([], function () {

    var form_name = 'charter_payment_common_form'    

    $_DO.cancel_charter_payment_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'charter_payments'}, {}, function (data) {

            data.__read_only = true

            var it = data.item            
                        
            $_F5 (data)

        })

    }
    
    $_DO.edit_charter_payment_common = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_charter_payment_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
                
        query ({type: 'charter_payments', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_charter_payment_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'charter_payments', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.download_charter_payment_common = function (e) {   
    
        var name = $(e.target).parent ().get (0).nextSibling.name
        
        var box = w2ui [form_name].box

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({

            type:   'charter_payment_docs', 
            id:     $('body').data ('data').item [name],
            action: 'download',

        }, {}, {

            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},

            onload: function () {w2utils.unlock (box)},

        })

    }
        
    $_DO.approve_charter_payment_common = function (e) {

        query ({type: 'service_payments', id: undefined}, {offset:0, limit: 10000, data: {uuid_charter_payment: $_REQUEST.id}}, function (d) {
        
            $.each (d.tb_svc_payments, function () {
                if (parseFloat(this.servicepaymentsize) >= 0.0001) return
                die ('foo', this ['w.label'] + ': не определена стоимость') 
            }) 
            
            if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
            
            query ({type: 'charter_payments', action: 'approve'}, {}, reload_page)

        })

    }
    
    $_DO.alter_charter_payment_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'charter_payments', action: 'alter'}, {}, reload_page)
    }
    
    $_DO.annul_charter_payment_common = function (e) {
        use.block ('charter_payment_annul_popup')
    }
    
    $_DO.create_house_charter_payment_common = function (e) {
        use.block ('charter_payment_new_house_passport')
    }
    
    $_DO.choose_tab_charter_payment_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('charter_payment_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('charter_payment_common.active_tab') || 'charter_payment_common_service_payments'

        data.__read_only = 1
        
        var it = data.item
        
        it.status_label     = data.vc_gis_status [it.id_ctr_status]
        it.err_text = it ['out_soap.err_text']        
            
        done (data)
        
    }

})