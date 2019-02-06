define ([], function () {

    var form_name = 'supply_resource_contract_common_form'

    $_DO.cancel_supply_resource_contract_common = function (e) {

        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record

        query ({type: 'supply_resource_contracts'}, {}, function (data) {

            data.__read_only = true

            var it = data.item

            $_F5 (data)

        })

    }

    $_DO.edit_supply_resource_contract_common = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false

        var $form = w2ui [form_name]

        $_F5 (data)

    }

    $_DO.update_supply_resource_contract_common = function (e) {

        if (!confirm ('Сохранить изменения?')) return

        var f = w2ui [form_name]

        var v = f.values ()

        if (!v.contractnumber) die ('contractnumber', 'Вы забыли указать номер договора')
        if (!v.signingdate) die ('signingdate', 'Вы забыли указать дату заключения')
        if (v.signingdate > new Date ().toISOString ()) die ('signingdate', 'Дата договора не может находиться в будущем')

        if (!v.effectivedate) die ('effectivedate', 'Вы забыли указать дату вступления в силу')

        if (v.effectivedate > v.completiondate) die ('completiondate', 'Дата окончания не может предшествовать дате начала')

        query ({type: 'supply_resource_contracts', action: 'update'}, {data: v}, reload_page)

    }

    $_DO.delete_supply_resource_contract_common = function (e) {
        if (!confirm ('Удалить эту запись, Вы уверены?')) return
        query ({type: 'supply_resource_contracts', action: 'delete'}, {}, reload_page)
    }

    $_DO.approve_supply_resource_contract_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'supply_resource_contracts', action: 'approve'}, {}, reload_page)
    }

    $_DO.alter_supply_resource_contract_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'supply_resource_contracts', action: 'alter'}, {data: {}}, reload_page)
    }

    $_DO.annul_supply_resource_contract_common = function (e) {
        use.block ('supply_resource_contract_annul_popup')
    }

    $_DO.choose_tab_supply_resource_contract_common = function (e) {

        var name = e.tab.id

        var layout = w2ui ['passport_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('supply_resource_contract_common.active_tab', name)

        use.block (name)

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('supply_resource_contract_common.active_tab') || 'supply_resource_contract_common_log'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']

        var it = data.item

        it.status_label     = data.vc_gis_status [it.id_ctr_status]
        it.state_label      = data.vc_gis_status [it.id_ctr_state]

        if (it.id_ctr_status != 10) {
            if (it.id_ctr_status != it.id_ctr_status_gis) it.gis_status_label = data.vc_gis_status [it.id_ctr_status_gis]
            it.gis_state_label  = data.vc_gis_status [it.id_ctr_state_gis]
        }

        it.err_text = it ['out_soap.err_text']

        if (it.id_ctr_status_gis == 110) it.is_annuled = 1

        if (it.isgratuitousbasis == 0 && it.other) it.isgratuitousbasis = -1

        done (data)

    }

})