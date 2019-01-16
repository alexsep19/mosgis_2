define ([], function () {

    $_DO.update_supply_resource_contract_subjects_new = function (e) {

        var form = w2ui ['supply_resource_contract_subjects_new_form']

        var v = form.values ()

        var it = $('body').data ('data').item

        if (!v.startsupplydate)
            die ('startsupplydate', 'Укажите, пожалуйста, дату начала поставки ресурса')

        if (v.endsupplydate && v.endsupplydate < v.startsupplydate)
            die ('endsupplydate', 'Дата начала поставки ресурса превышает дату окончания поставки ресурса')

        v.uuid_sr_ctr = $_REQUEST.id

        query ({type: 'supply_resource_contract_subjects', action: 'create', id: undefined}, {data: v}, function (data) {

            w2popup.close ()

            if (data.id)
                w2confirm ('Предмет договора ресурсоснабэения зарегистрирован. Открыть его страницу в новой вкладке?')
                    .yes (function () {openTab ('/supply_resource_contract_subject/' + data.id)})

            var grid = w2ui ['supply_resource_contract_subjects_grid']

            grid.reload (grid.refresh)

        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = {
            uuid_sr_ctr: $_REQUEST.id,
            startsupplydate: dt_dmy (data.item.effectivedate),
            endsupplydate:   dt_dmy (data.item.plandatecomptetion),
        }

        query ({type: 'supply_resource_contract_subjects', part: 'vocs', id: undefined}, {}, function (d) {

            add_vocabularies (d, {
                vc_nsi_239: 1,
                vc_nsi_3: 1,
                vc_okei: 1
            })

            for (var k in d) {
                data[k] = d[k]
            }

            done (data)
        })

    }

})