define ([], function () {

    var form_name = 'interval_common_form'
    
    $_DO.approve_interval_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'intervals', action: 'approve'}, {}, reload_page)
    }
        
    $_DO.alter_interval_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'intervals', action: 'alter'}, {}, reload_page)
    }    
 
    $_DO.cancel_interval_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'intervals'}, {}, function (data) {

            data.__read_only = true
            
            var it = data.item

            $_F5 (data)

        })

    }
    
    $_DO.edit_interval_common = function (e) {

        var data = {item: w2ui [form_name].record}

        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_interval_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
        
        if (!v.code_vc_nsi_3)
            die('code_vc_nsi_3', 'Укажите, пожалуйста, вид коммунальной услуги')
        if (!v.code_vc_nsi_239)
            die('code_vc_nsi_239', 'Укажите, пожалуйста, вид коммунального ресурса')
        if (!v.startdateandtime)
            die('startdateandtime', 'Укажите, пожалуйста, дату и время начала перерыва')

        query ({type: 'intervals', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_interval_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'intervals', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_interval_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('interval_common.active_tab', name)
            
        use.block (name)        
    
    }

    function fix (it) {

        it.status_label = $('body').data ('data').vc_gis_status [it.id_ctr_status]            

        it.err_text = it ['out_soap.err_text']
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('interval_common.active_tab') || 'interval_common_log'

        data.__read_only = 1
        
        var it = data.item
        
        fix (it)
                
        var v = {
            uuid_contract: it.uuid_contract,
            uuid_sr_ctr: it.uuid_sr_ctr,
            uuid_charter_object: it.uuid_charter_object
        }

        var tia = {type: 'supply_resource_contract_subjects', id: undefined}

        query(tia, {limit: 10000, offset: 0, data: v}, function (d) {

            data.service2resource = {}

            $.each(d.tb_sr_ctr_subj, function () {
                data.service2resource[this.code_vc_nsi_3] = data.service2resource[this.code_vc_nsi_3] || []
                data.service2resource[this.code_vc_nsi_3].push({
                    id: this.code_vc_nsi_239,
                    text: data.vc_nsi_239[this.code_vc_nsi_239]
                })
            })
            data.vc_nsi_3.items = data.vc_nsi_3.items.filter(function (i) {
                return !!data.service2resource[i.id]
            })
            data.vc_nsi_239.items = data.service2resource[it.code_vc_nsi_3]

            done(data)
        })
        
    }

})