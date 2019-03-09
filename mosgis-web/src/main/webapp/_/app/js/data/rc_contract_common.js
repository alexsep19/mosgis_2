define ([], function () {

    var form_name = 'rc_contract_common_form'

    $_DO.cancel_rc_contract_common = function (e) {

        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record

        query ({type: 'rc_contracts'}, {}, function (data) {

            data.__read_only = true

            var it = data.item

            $_F5 (data)

        })

    }

    $_DO.edit_rc_contract_common = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false

        var $form = w2ui [form_name]

        $_F5 (data)

    }

    $_DO.update_rc_contract_common = function (e) {

        if (!confirm ('Сохранить изменения?')) return

        var f = w2ui [form_name]

        var v = f.values ()

        if (!v.contractnumber) die ('contractnumber', 'Вы забыли указать номер договора')
        if (!v.signingdate) die ('signingdate', 'Вы забыли указать дату заключения')
        if (!v.effectivedate) die ('effectivedate', 'Вы забыли указать дату вступления в силу')

        query ({type: 'rc_contracts', action: 'update'}, {data: v}, reload_page)

    }

    $_DO.delete_rc_contract_common = function (e) {
        if (!confirm ('Удалить эту запись, Вы уверены?')) return
        query ({type: 'rc_contracts', action: 'delete'}, {}, reload_page)
    }

    $_DO.approve_rc_contract_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'rc_contracts', action: 'approve'}, {}, reload_page)
    }

    $_DO.alter_rc_contract_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'rc_contracts', action: 'alter'}, {data: {}}, reload_page)
    }

    $_DO.annul_rc_contract_common = function (e) {
        use.block ('supply_resource_contract_annul_popup')
    }
    
    $_DO.set_bank_acct_rc_contract_common = function (e) {   
        use.block ('bank_acct_select_popup')
    }

    $_DO.choose_tab_rc_contract_common = function (e) {

        var name = e.tab.id

        var layout = w2ui ['passport_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('rc_contract_common.active_tab', name)

        use.block (name)

    }

    $_DO.open_orgs_rc_contract_common = function (e) {

        var f = w2ui [form_name]

        var saved = {
            data: clone($('body').data('data')),
            record: clone(f.record)
        }

        function done() {
            $('body').data('data', saved.data)
            f.record.uuid_org = saved.record.uuid_org
            f.record.label_org = saved.record.label_org
            f.refresh()
        }

        $('body').data('voc_organizations_popup.callback', function (r) {

            if (!r)
                return done()

            saved.record.uuid_org = r.uuid
            saved.record.label_org = r.label

            done()

        })

        var search = [{
            field: "code_vc_nsi_20",
            operator: "in",
            type: "enum",
            value: [
                {
                    "id": "36",
                    "text": "РЦ"
                }
            ]
        }]

        $_SESSION.set('voc_organization_popup.post_data', {search: search, searchLogic: 'AND'})

        use.block('voc_organizations_popup')
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('rc_contract_common.active_tab') || 'rc_contract_common_log'

        var it = data.item

        data.__read_only = it.__read_only = true

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']

        it.status_label     = data.vc_gis_status [it.id_ctr_status]
        it.state_label      = data.vc_gis_status [it.id_ctr_state]

        if (it.id_ctr_status != 10) {
            if (it.id_ctr_status != it.id_ctr_status_gis) it.gis_status_label = data.vc_gis_status [it.id_ctr_status_gis]
            it.gis_state_label  = data.vc_gis_status [it.id_ctr_state_gis]
        }

        it.err_text = it ['out_soap.err_text']

        if (it.id_ctr_status_gis == 110) it.is_annuled = 1

        done (data)

    }

})