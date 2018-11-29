define ([], function () {

    $_DO.patch_voc_organization_legal_info = function (e) {

        var grid = this

        var col = grid.columns [e.column]

        var editable = col.editable (grid.get (e.recid))

        var data = {
            k: e.recid,
            v: normalizeValue (e.value_new, editable.type)
        }

        if (data.v != null) data.v = String (data.v)

        grid.lock ()

        var tia = {type: 'voc_organizations', action: 'patch'}

        query (tia, {data: data}, function () {

            var voc_organization = $('body').data ('data')
            voc_organization.item [data.k] = data.v

            $('body').data ('data', voc_organization)

            grid.unlock ()
            $_F5 ()

        })

    }

    function org_vc_pass_fields(data) {

        var uo_rso_tsg_gsk_gk = ["1", "2", "19", "20", "21", "22"]

        data.is_on_self_manage = data.vc_orgs_nsi_20
            .filter(function(i) { return uo_rso_tsg_gsk_gk.indexOf(i.code) >= 0 })
            .length > 0

        var rows = [
            {"id_type" : 2, "id": "post_address", "label": "Почтовый адрес"},
            {"id_type": 2, "id": "phone", "label": "Телефон организации"},
            {"id_type": 2, "id": "mail", "label": "Электронный адрес"},
            {"id_type": 2, "id": "site", "label": "Адрес официального сайта"},
            {"id_type": 2, "id": "phone_support", "label": "Номер телефона горячей линии"},

            {"id": "h_head", "label": "Контакты руководителя"},
            {"id_type": 2, "id": "head_fio", "label": "ФИО руководителя"},
            {"id_type": 2, "id": "head_post", "label": "Должность руководителя"},
            {"id_type": 2, "id": "head_phone", "label": "Телефон руководителя"},
            {"id_type": 2, "id": "head_mail", "label": "Электронный адрес руководителя"},

            {"id": "h_vice", "label": "Контакты заместителя"},
            {"id_type": 2, "id": "vice_fio", "label": "ФИО заместителя"},
            {"id_type": 2, "id": "vice_post", "label": "Должность заместителя"},
            {"id_type": 2, "id": "vice_phone", "label": "Телефон заместителя"},
            {"id_type": 2, "id": "vice_mail", "label": "Электронный адрес заместителя"},

            {"id": "h_citizen", "label": "Прием граждан"},
            {"id_type": 2, "id": "citizen_address", "label": "Адрес приема граждан"},
            {"id_type": 2, "id": "citizen_phone", "label": "Телефон приема граждан"},
            {"id_type": 2, "id": "citizen_place", "label": "Место размещения информации для граждан"},

            {"id": "h_dispatch", "label": "Диспетчерская служба"},
            {"id_type": 2, "id": "dispatch_address", "label": "Адрес диспетчерской службы"},
            {"id_type": 2, "id": "dispatch_phone", "label": "Контактные телефоны"},
            {"id_type": 2, "id": "dispatch_schedule", "label": "Режим работы"},

            {"id": "h_self_manage", "label": "Саморегулируемая организация", "off": !data.is_on_self_manage},
            {"id_type": 2, "id": "self_manage_org", "label": "Наименование саморегулируемой организации", "off": !data.is_on_self_manage},
            {"id_type": 8, "id": "dt_from_self_manage_org", "label": "Дата вступления в члены организации", "off": !data.is_on_self_manage},
            {"id_type": 8, "id": "dt_to_self_manage_org", "label": "Дата исключения/выхода из членов организации", "off": !data.is_on_self_manage},
            {"id_type": 2, "id": "reason_cancel_self_manage_org", "label": "Причина исключения из членов организации", "off": !data.is_on_self_manage},

            {"id": "h_staff", "label": "Штатная численность"},
            {"id_type": 1, "id": "staff_cnt", "label": "Количество штатных единиц"},
            {"id_type": 1, "id": "staff_work_cnt", "label": "Количество работающих человек"}
        ]

        return rows.filter(not_off)
    }

    return function (done) {

        query ({type: 'voc_organizations'}, {}, function (data) {

            data.vc_pass_fields = org_vc_pass_fields(data)

            data.doc_fields = {}

            var fields = data.vc_pass_fields

            var vocs = {}

            $.each (fields, function () {

                this.recid = this.id

                this.name = this.id

                this.value = data.item [this.name]

                if (this.is_mandatory) this.w2ui = {class: 'status_warning'}

                if (this.voc) {

                    var name = 'vc_nsi_' + this.voc

                    if (data [name]) vocs [name] = 1

                }

                if (this.id_type == 3) {
                    data.doc_fields [this.id] = 1
                    data.doc_fields [this.id_dt] = 1
                    data.doc_fields [this.id_no] = 1
                }

            })

            add_vocabularies (data, vocs)

            data.vc_pass_fields = fields

            $('body').data('data', data)

            done (data);

        })

        w2ui ['voc_organization_legal_layout'].unlock ('main')
    }

})